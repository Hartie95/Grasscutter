package emu.grasscutter.game.world;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.data.excels.SceneData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.Player.SceneLoadState;
import emu.grasscutter.game.props.EnterReason;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.props.PlayerProperty;
import emu.grasscutter.game.props.SceneType;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.world.data.TeleportProperties;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.proto.EnterTypeOuterClass.EnterType;
import emu.grasscutter.scripts.data.SceneConfig;
import emu.grasscutter.server.event.player.PlayerTeleportEvent;
import emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.server.packet.send.*;
import emu.grasscutter.utils.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType.SCRIPT;

public class World implements Iterable<Player> {
    private final GameServer server;
    private final Player owner;
    private final List<Player> players;
    private final Int2ObjectMap<Scene> scenes;

    private int levelEntityId;
    private int nextEntityId = 0;
    private int nextPeerId = 0;
    private int worldLevel;

    private boolean isMultiplayer;


    @Getter
    private int tickCount = 0;
    @Getter private boolean isPaused = false;
    @Getter private boolean isGameTimeLocked = false;
    private long lastUpdateTime;
    @Getter private long currentWorldTime = 0;

    @Getter private long currentGameTime = 540;

    public World(Player player) {
        this(player, false);
    }

    public World(Player player, boolean isMultiplayer) {
        this.owner = player;
        this.server = player.getServer();
        this.players = Collections.synchronizedList(new ArrayList<>());
        this.scenes = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

        this.levelEntityId = this.getNextEntityId(EntityIdType.MPLEVEL);
        this.worldLevel = player.getWorldLevel();
        this.isMultiplayer = isMultiplayer;
        this.lastUpdateTime = System.currentTimeMillis();
        this.currentWorldTime = owner.getPlayerGameTime();
        this.isGameTimeLocked = owner.getBoolProperty(PlayerProperty.PROP_IS_GAME_TIME_LOCKED);

        this.owner.getServer().registerWorld(this);
    }

    public boolean setGameTimeLocked(boolean gameTimeLocked) {
        isGameTimeLocked = gameTimeLocked;
        getPlayers().forEach(p -> p.setProperty(PlayerProperty.PROP_IS_GAME_TIME_LOCKED, gameTimeLocked));
        return true;
    }

    public Player getHost() {
        return owner;
    }

    public GameServer getServer() {
        return server;
    }

    public int getLevelEntityId() {
        return levelEntityId;
    }

    public int getHostPeerId() {
        if (this.getHost() == null) {
            return 0;
        }
        return this.getHost().getPeerId();
    }

    public int getNextPeerId() {
        return ++this.nextPeerId;
    }

    public int getWorldLevel() {
        return worldLevel;
    }

    public void setWorldLevel(int worldLevel) {
        this.worldLevel = worldLevel;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Int2ObjectMap<Scene> getScenes() {
        return this.scenes;
    }

    public Scene getSceneById(int sceneId) {
        // Get scene normally
        Scene scene = this.getScenes().get(sceneId);
        if (scene != null) {
            return scene;
        }

        // Create scene from scene data if it doesnt exist
        SceneData sceneData = GameData.getSceneDataMap().get(sceneId);
        if (sceneData != null) {
            scene = new Scene(this, sceneData);
            this.registerScene(scene);
            return scene;
        }

        return null;
    }

    public int getPlayerCount() {
        return this.getPlayers().size();
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public int getNextEntityId(EntityIdType idType) {
        return (idType.getId() << 24) + ++this.nextEntityId;
    }

    public synchronized void addPlayer(Player player) {
        // Check if player already in
        if (this.getPlayers().contains(player)) {
            return;
        }

        // Remove player from prev world
        if (player.getWorld() != null) {
            player.getWorld().removePlayer(player);
        }

        // Register
        player.setWorld(this);
        this.getPlayers().add(player);

        // Set player variables
        player.setPeerId(this.getNextPeerId());
        player.getTeamManager().setEntityId(this.getNextEntityId(EntityIdType.TEAM));

        // Copy main team to multiplayer team
        if (this.isMultiplayer()) {
            player.getTeamManager().getMpTeam().copyFrom(player.getTeamManager().getCurrentSinglePlayerTeamInfo(), player.getTeamManager().getMaxTeamSize());
            player.getTeamManager().setCurrentCharacterIndex(0);
        }

        // Add to scene
        Scene scene = this.getSceneById(player.getSceneId());
        scene.addPlayer(player);

        // Info packet for other players
        if (this.getPlayers().size() > 1) {
            this.updatePlayerInfos(player);
        }
    }

    public synchronized void removePlayer(Player player) {
        // Remove team entities
        player.sendPacket(
                new PacketDelTeamEntityNotify(
                        player.getSceneId(),
                    this.getPlayers().stream().map(p -> p.getTeamManager().getEntityId()).collect(Collectors.toList())
                )
        );

        // Deregister
        this.getPlayers().remove(player);
        player.setWorld(null);

        // Remove from scene
        Scene scene = this.getSceneById(player.getSceneId());
        scene.removePlayer(player);

        // Info packet for other players
        if (this.getPlayers().size() > 0) {
            this.updatePlayerInfos(player);
        }

        // Disband world if host leaves
        if (this.getHost() == player) {
            List<Player> kicked = new ArrayList<>(this.getPlayers());
            for (Player victim : kicked) {
                World world = new World(victim);
                world.addPlayer(victim);

                victim.sendPacket(new PacketPlayerEnterSceneNotify(victim, EnterType.ENTER_TYPE_SELF, EnterReason.TeamKick, victim.getSceneId(), victim.getPosition()));
            }
        }
    }

    public void registerScene(Scene scene) {
        this.getScenes().put(scene.getId(), scene);
    }

    public void deregisterScene(Scene scene) {
        scene.saveGroups();
        this.getScenes().remove(scene.getId());
    }

    public void save() {
        this.getScenes().values().forEach(Scene::saveGroups);
    }

    public boolean transferPlayerToScene(Player player, int sceneId, Position pos) {
        return this.transferPlayerToScene(player, sceneId, TeleportType.INTERNAL, null, pos);
    }

    public boolean transferPlayerToScene(Player player, int sceneId, TeleportType teleportType, Position pos) {
        return this.transferPlayerToScene(player, sceneId, teleportType, null, pos);
    }

    public boolean transferPlayerToScene(Player player, int sceneId, DungeonData data) {
        return this.transferPlayerToScene(player, sceneId, TeleportType.DUNGEON, data, null);
    }

    public boolean transferPlayerToScene(Player player, int sceneId, TeleportType teleportType, DungeonData dungeonData, Position teleportTo) {
        EnterReason enterReason = switch (teleportType) {
            // shouldn't affect the teleportation, but its clearer when inspecting the packets
            // TODO add more conditions for different reason.
            case INTERNAL -> EnterReason.TransPoint;
            case WAYPOINT -> EnterReason.TransPoint;
            case MAP -> EnterReason.TransPoint;
            case COMMAND -> EnterReason.Gm;
            case SCRIPT -> EnterReason.Lua;
            case CLIENT -> EnterReason.ClientTransmit;
            case DUNGEON -> EnterReason.DungeonEnter;
            default -> EnterReason.None;
        };
        return transferPlayerToScene(player, sceneId, teleportType, enterReason, dungeonData, teleportTo);
    }


    public boolean transferPlayerToScene(Player player, int sceneId, TeleportType teleportType, EnterReason enterReason, DungeonData dungeonData, Position teleportTo) {
        // Get enter types
        val teleportProps = TeleportProperties.builder()
            .sceneId(sceneId)
            .teleportType(teleportType)
            .enterReason(enterReason)
            .teleportTo(teleportTo)
            .enterType(EnterType.ENTER_TYPE_JUMP);

        val sceneData = GameData.getSceneDataMap().get(sceneId);
        if (dungeonData != null) {
            teleportProps.enterType(EnterType.ENTER_TYPE_DUNGEON)
                .enterReason(EnterReason.DungeonEnter);
        } else if (player.getSceneId() == sceneId) {
            teleportProps.enterType(EnterType.ENTER_TYPE_GOTO);
        } else if (sceneData!= null && sceneData.getSceneType() == SceneType.SCENE_HOME_WORLD) {
            // Home
            teleportProps.enterType(EnterType.ENTER_TYPE_SELF_HOME)
                .enterReason(EnterReason.EnterHome);
        }
        return transferPlayerToScene(player, teleportProps.build());
    }

    public boolean transferPlayerToScene(Player player, TeleportProperties teleportProperties) {
        // Call player teleport event.
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, teleportProperties, player.getPosition());
        // Call event & check if it was canceled.
        event.call(); if (event.isCanceled()) {
            return false; // Teleport was canceled.
        }

        if (GameData.getSceneDataMap().get(teleportProperties.getSceneId()) == null) {
            return false;
        }

        Scene oldScene = null;

        if (player.getScene() != null) {
            oldScene = player.getScene();

            // Don't deregister scenes if the player is going to tp back into them
            if (oldScene.getId() == teleportProperties.getSceneId()) {
                oldScene.setDontDestroyWhenEmpty(true);
            }

            oldScene.removePlayer(player);
        }

        Scene newScene = this.getSceneById(teleportProperties.getSceneId());
        newScene.addPlayer(player);
        player.setAvatarsAbilityForScene(newScene);
        // Dungeon
        // Dungeon system is handling this already
        // if(dungeonData!=null){
        //     var dungeonManager = new DungeonManager(newScene, dungeonData);
        //     dungeonManager.startDungeon();
        // }
        SceneConfig config = newScene.getScriptManager().getConfig();
        if (teleportProperties.getTeleportTo() == null && config != null) {
            if (config.born_pos != null) {
                teleportProperties.setTeleportTo(newScene.getScriptManager().getConfig().born_pos);
            }
            if (config.born_rot != null) {
                teleportProperties.setTeleportRot(config.born_rot);
            }
        }

        // Set player position and rotation
        if(teleportProperties.getTeleportTo() != null) {
            player.getPosition().set(teleportProperties.getTeleportTo());
        }
        if(teleportProperties.getTeleportRot()!=null) {
            player.getRotation().set(teleportProperties.getTeleportRot());
        }

        if (oldScene != null && newScene != oldScene) {
            newScene.setPrevScene(oldScene.getId());
            oldScene.setDontDestroyWhenEmpty(false);
        }


        // Teleport packet
        player.sendPacket(new PacketPlayerEnterSceneNotify(player, teleportProperties));

        if(teleportProperties.getTeleportType() != TeleportType.INTERNAL && teleportProperties.getTeleportType() != SCRIPT) {
            player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_ANY_MANUAL_TRANSPORT);
        }
        return true;
    }

    private void updatePlayerInfos(Player paramPlayer) {
        for (Player player : this.getPlayers()) {
            // Dont send packets if player is logging in and filter out joining player
            if (!player.hasSentLoginPackets() || player == paramPlayer) {
                continue;
            }

            // Update team of all players since max players has been changed - Probably not the best way to do it
            if (this.isMultiplayer()) {
                player.getTeamManager().getMpTeam().copyFrom(player.getTeamManager().getMpTeam(), player.getTeamManager().getMaxTeamSize());
                player.getTeamManager().updateTeamEntities(true);
            }

            // Dont send packets if player is loading into the scene
            if (player.getSceneLoadState().getValue() < SceneLoadState.INIT.getValue() ) {
                // World player info packets
                player.getSession().send(new PacketWorldPlayerInfoNotify(this));
                player.getSession().send(new PacketScenePlayerInfoNotify(this));
                player.getSession().send(new PacketWorldPlayerRTTNotify(this));

                // Team packets
                player.getSession().send(new PacketSyncTeamEntityNotify(player));
                player.getSession().send(new PacketSyncScenePlayTeamEntityNotify(player));
            }
        }
    }

    public void broadcastPacket(BasePacket packet) {
        // Send to all players - might have to check if player has been sent data packets
        for (Player player : this.getPlayers()) {
            player.getSession().send(packet);
        }
    }

    // Returns true if the world should be deleted
    public boolean onTick() {
        if (this.getPlayerCount() == 0) return true;
        this.scenes.forEach((k, scene) -> scene.onTick());

        if(!isGameTimeLocked && !isPaused){
            currentGameTime++;
        }


        // sync time every 10 seconds
        if(tickCount%10 == 0){
            players.forEach(p -> p.sendPacket(new PacketPlayerGameTimeNotify(p)));
        }
        // store updated world time every 60 seconds (ingame hour)
        if(tickCount%60 == 0){
            this.owner.updatePlayerGameTime(currentWorldTime);
        }
        tickCount++;
        return false;
    }

    public void close() {

    }

    public void changeTime(int time, int days) {
        val currentTime = getGameTime();
        var diff = time - currentTime;
        if(diff < 0){
            diff = 1440 + diff;
        }
        this.currentGameTime += days * 1440L + diff;
        this.owner.updatePlayerGameTime(currentGameTime);
        this.players.forEach(player -> player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_GAME_TIME_TICK,
            getGameTimeHours(), // hours
            days)); //days
    }

    public void setPaused(boolean paused) {
        getWorldTime();
        if(this.isPaused != paused && !paused){
            this.lastUpdateTime = System.currentTimeMillis();
        }
        isPaused = paused;
        players.forEach(player -> player.setPaused(paused));
        scenes.forEach((key, scene) -> scene.setPaused(paused));
    }

    public static long getDaysForGameTime(long inGameMinutes){
        return inGameMinutes / 1440;
    }

    public static long getHoursForGameTime(long inGameMinutes){
        return inGameMinutes / 60;
    }

    /**
     * Returns the current in game days world time in ingame minutes (0-1439)
     */
    public int getGameTime() {
        return (int)(currentGameTime % 1440);
    }

    /**
     * Returns the current in game days world time in ingame hours (0-23)
     */
    public int getGameTimeHours() {
        return getGameTime() / 60 ;
    }

    /**
     * Returns the total number of in game days that got completed since the beginning of the game
     */
    public long getTotalGameTimeDays() {
        return getDaysForGameTime(getTotalGameTimeMinutes());
    }

    /**
     * Returns the total number of in game hours that got completed since the beginning of the game
     */
    public long getTotalGameTimeHours() {
        return getHoursForGameTime(getTotalGameTimeMinutes());
    }

    /**
     * Returns the total amount of ingame minutes that got completed since the beginning of the game
     */
    public long getTotalGameTimeMinutes() {
        return currentGameTime;
    }

    /**
     * Returns the ingame world time in irl millis
     */
    public long getWorldTime() {
        if(!isPaused) {
            long newUpdateTime = System.currentTimeMillis();
            this.currentWorldTime += (newUpdateTime - lastUpdateTime);
            this.lastUpdateTime = newUpdateTime;
        }
        return currentWorldTime;
    }

    @Override
    public Iterator<Player> iterator() {
        return this.getPlayers().iterator();
    }
}
