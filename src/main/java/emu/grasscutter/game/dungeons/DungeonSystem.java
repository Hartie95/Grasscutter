package emu.grasscutter.game.dungeons;

import emu.grasscutter.GameConstants;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.common.DungeonEntries;
import emu.grasscutter.data.common.PointData;
import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.data.excels.DungeonPassConfigData;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.enums.DungeonType;
import emu.grasscutter.game.dungeons.handlers.DungeonBaseHandler;
import emu.grasscutter.game.dungeons.pass_condition.BaseCondition;
import emu.grasscutter.game.dungeons.settle_listeners.BasicDungeonSettleListener;
import emu.grasscutter.game.dungeons.settle_listeners.DungeonSettleListener;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.SceneType;
import emu.grasscutter.server.game.BaseGameSystem;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.utils.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.val;
import org.reflections.Reflections;

import java.util.Optional;

public class DungeonSystem extends BaseGameSystem {
    private static final BasicDungeonSettleListener BASIC_DUNGEON_SETTLE_LISTENER = new BasicDungeonSettleListener();
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
        return enterDungeon(player, pointId, dungeonId, BASIC_DUNGEON_SETTLE_LISTENER);
    }

    /**
     * TODO records also player's position for dungeon that does not have entries and exits,
     * like trial avatar activity and mist trial activity
     * */
    public boolean enterDungeon(Player player, int pointId, int dungeonId, DungeonSettleListener dungeonSettleListeners) {
        val data = GameData.getDungeonDataMap().get(dungeonId);
        if (data == null) {
            Grasscutter.getLogger().error("No resource found for this dungeon: {}", dungeonId);
            return false;
        }

        Grasscutter.getLogger().info("{}({}) is trying to enter {}({})", player.getNickname(), player.getUid(), data.getType(), dungeonId);
        player.getScene().setPrevScene(player.getSceneId());
        if (player.getWorld().transferPlayerToScene(player, data.getSceneId(), data)) {
            player.getScene().setDungeonManager(new DungeonManager(player.getScene(), data));
            player.getScene().addDungeonSettleObserver(dungeonSettleListeners);
        }

        player.getScene().setPrevScenePoint(Optional.ofNullable(GameData.getDungeonEntriesMap().get(dungeonId))
            .map(DungeonEntries::getEntryPoint).map(PointData::getId).orElse(pointId));
        return true;
    }

    public void restartDungeon(Player player) {
        val scene = player.getScene();
        if (scene == null || scene.getDungeonManager() == null) return;

        scene.getScriptManager().onDestroy();
        scene.getWorld().deregisterScene(scene);
        enterDungeon(player, 0, scene.getDungeonManager().getDungeonData().getId());
    }

    /**
     * Remove player from dungeon
     * */
    public void exitDungeon(Player player, boolean isQuitImmediately) {
        val scene = player.getScene();
        if (scene == null || scene.getSceneType() != SceneType.SCENE_DUNGEON) return;

        val dungeonManager = scene.getDungeonManager();
        val dungeonData = Optional.ofNullable(dungeonManager).map(DungeonManager::getDungeonData).orElse(null);
        // Get dungeon exit point
        val exitPoint = Optional.ofNullable(dungeonData).map(data -> GameData.getDungeonEntriesMap().get(data.getId()))
            .map(DungeonEntries::getExitPoint);
        val newPos = exitPoint.map(PointData::getTranPos).orElse(new Position(GameConstants.START_POSITION));
        val newRot = exitPoint.map(PointData::getTranRot).orElse(null);
        int pointId = exitPoint.map(PointData::getId).orElse(0);
        int delayExitTime = -1;

        if(dungeonData != null && !dungeonManager.isFinishedSuccessfully() && dungeonManager.getDelayExitTaskId() < 0) {
            // fail challenges if exist
            val challenge = Optional.ofNullable(scene.getChallenge()).filter(WorldChallenge::inProgress);
            challenge.ifPresent(WorldChallenge::fail);
            if (challenge.isPresent()) {
                delayExitTime = dungeonData.getFailSettleCountdownTime();
                dungeonManager.failDungeon();
            } else {
                delayExitTime = dungeonData.getQuitSettleCountdownTime();
                dungeonManager.quitDungeon();
            }
        }
        // if player is leaving from tower
        if (Optional.ofNullable(dungeonData).map(DungeonData::getType)
            .filter(type -> type == DungeonType.DUNGEON_TOWER).isPresent()) {
            player.getTowerManager().restoreEnergy();
            player.getTowerManager().removeCurrentLevelBuff();
            player.getTeamManager().cleanTemporaryTeam();
            isQuitImmediately = true;
        }

        // remove any existing transfer task before scheduling new one
        Optional.ofNullable(dungeonManager).filter(m -> m.getDelayExitTaskId() > 0).ifPresent(m -> {
                Grasscutter.getGameServer().getScheduler().cancelTask(m.getDelayExitTaskId());
                m.setDelayExitTaskId(-1);
            });

        Runnable transferTask = () -> {
            scene.setPrevScene(scene.getId());
            player.getWorld().transferPlayerToScene(player, exitPoint.map(PointData::getSceneId).orElse(3), newPos, newRot);
            player.getScene().setPrevScenePoint(pointId);
        };

        // Transfer player back to world
        if (isQuitImmediately) {
            transferTask.run();
        } else {
            int delayTaskId = Grasscutter.getGameServer().getScheduler().scheduleDelayedTask(transferTask, delayExitTime);
            Optional.ofNullable(dungeonManager).ifPresent(m -> m.setDelayExitTaskId(delayTaskId));
        }
    }
}
