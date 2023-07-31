package emu.grasscutter.game.dungeons;

import emu.grasscutter.GameConstants;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScenePointEntry;
import emu.grasscutter.data.common.PointData;
import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.data.excels.DungeonPassConfigData;
import emu.grasscutter.game.dungeons.handlers.DungeonBaseHandler;
import emu.grasscutter.game.dungeons.pass_condition.BaseCondition;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.SceneType;
import emu.grasscutter.server.game.BaseGameSystem;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.utils.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.val;
import org.reflections.Reflections;

import java.util.List;
import java.util.Optional;

public class DungeonSystem extends BaseGameSystem {
    private static final BasicDungeonSettleListener basicDungeonSettleObserver = new BasicDungeonSettleListener();
    private final Int2ObjectMap<DungeonBaseHandler> passCondHandlers;

    public DungeonSystem(GameServer server) {
        super(server);
        this.passCondHandlers = new Int2ObjectOpenHashMap<>();
        registerHandlers();
    }

    public void registerHandlers() {
        registerHandlers(this.passCondHandlers, BaseCondition.class.getPackageName(), DungeonBaseHandler.class);
    }

    public <T> void registerHandlers(Int2ObjectMap<T> map, String packageName, Class<T> clazz) {
        new Reflections(packageName).getSubTypesOf(clazz).forEach(obj -> registerPacketHandler(map, obj));
    }

    public <T> void registerPacketHandler(Int2ObjectMap<T> map, Class<? extends T> handlerClass) {
        Optional.ofNullable(handlerClass.getAnnotation(DungeonValue.class))
            .map(DungeonValue::value)
            .ifPresent(value ->  {
                try {
                    map.put(value.ordinal(), handlerClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    public boolean triggerCondition(DungeonPassConfigData.DungeonPassCondition condition, int... params) {
        val handler = this.passCondHandlers.get(condition.getCondType().ordinal());

        if (handler == null) {
            Grasscutter.getLogger().debug("Could not trigger condition {} at {}", condition.getCondType(), params);
            return false;
        }
        return handler.execute(condition, params);
    }

    public boolean enterDungeon(Player player, int pointId, int dungeonId) {
        DungeonData data = GameData.getDungeonDataMap().get(dungeonId);
        if (data == null) return false;

        Grasscutter.getLogger().info("{}({}) is trying to enter dungeon {}", player.getNickname(), player.getUid(), dungeonId);
        player.getScene().setPrevScene(data.getSceneId());
        if (player.getWorld().transferPlayerToScene(player, data.getSceneId(), data)) {
            player.getScene().setDungeonManager(new DungeonManager(player.getScene(), data));
            player.getScene().addDungeonSettleObserver(basicDungeonSettleObserver);
        }

        player.getScene().setPrevScenePoint(pointId);
        return true;
    }

    /**
     * used in tower dungeons handoff
     */
    public boolean handoffDungeon(Player player, int dungeonId, List<DungeonSettleListener> dungeonSettleListeners) {
        val data = GameData.getDungeonDataMap().get(dungeonId);

        if (data == null) return false;

        Grasscutter.getLogger().info("{}({}) is trying to enter tower dungeon {}" ,player.getNickname(),player.getUid(),dungeonId);
        if (player.getWorld().transferPlayerToScene(player, data.getSceneId(), data)) {
            dungeonSettleListeners.forEach(player.getScene()::addDungeonSettleObserver);
        }
        return true;
    }

    public void exitDungeon(Player player) {
        val scene = player.getScene();
        if (scene == null || scene.getSceneType() != SceneType.SCENE_DUNGEON) return;

        // Get previous scene
        int prevScene = scene.getPrevScene() > 0 ? scene.getPrevScene() : 3;

        // Get previous position
        val dungeonManager = scene.getDungeonManager();
        DungeonData dungeonData = Optional.ofNullable(dungeonManager).map(DungeonManager::getDungeonData).orElse(null);
        Position prevPos = new Position(GameConstants.START_POSITION);

        if (dungeonData != null) {
            Optional.ofNullable(GameData.getScenePointEntryById(prevScene, scene.getPrevScenePoint()))
                .map(ScenePointEntry::getPointData).map(PointData::getTranPos).ifPresent(prevPos::set);

            if(!dungeonManager.isFinishedSuccessfully()) dungeonManager.quitDungeon();
        }
        // clean temp team if it has
        player.getTeamManager().cleanTemporaryTeam();
        player.getTowerManager().clearEntry();

        // Transfer player back to world
        player.getWorld().transferPlayerToScene(player, prevScene, prevPos);
    }
}
