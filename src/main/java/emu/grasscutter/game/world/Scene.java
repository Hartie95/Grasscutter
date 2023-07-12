package emu.grasscutter.game.world;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameDepot;
import emu.grasscutter.data.binout.SceneNpcBornEntry;
import emu.grasscutter.data.binout.routes.Route;
import emu.grasscutter.data.excels.*;
import emu.grasscutter.data.server.Grid;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.DungeonSettleListener;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.entity.gadget.GadgetWorktop;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.*;
import emu.grasscutter.game.quest.QuestGroupSuite;
import emu.grasscutter.game.world.data.TeleportProperties;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.proto.AttackResultOuterClass.AttackResult;
import emu.grasscutter.net.proto.EnterTypeOuterClass;
import emu.grasscutter.net.proto.SelectWorktopOptionReqOuterClass;
import emu.grasscutter.net.proto.VisionTypeOuterClass.VisionType;
import emu.grasscutter.scripts.SceneIndexManager;
import emu.grasscutter.scripts.SceneScriptManager;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneBlock;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.event.player.PlayerTeleportEvent;
import emu.grasscutter.server.packet.send.*;
import emu.grasscutter.utils.KahnsSort;
import emu.grasscutter.utils.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


public class Scene {
    @Getter private final World world;
    @Getter private final SceneData sceneData;
    @Getter private final List<Player> players;
    @Getter private final Map<Integer, GameEntity> entities;
    @Getter private final Set<SpawnDataEntry> spawnedEntities;
    @Getter private final Set<SpawnDataEntry> deadSpawnedEntities;
    @Getter private final Set<SceneBlock> loadedBlocks;
    @Getter private final Set<SceneGroup> loadedGroups;
    private final HashSet<Integer> unlockedForces;
    private final List<Runnable> afterLoadedCallbacks = new ArrayList<>();
    private final long startWorldTime;
    @Getter @Setter DungeonManager dungeonManager;
    @Getter Int2ObjectMap<Route> sceneRoutes;
    private Set<SpawnDataEntry.GridBlockId> loadedGridBlocks;
    @Getter @Setter private boolean dontDestroyWhenEmpty;
    @Getter private final SceneScriptManager scriptManager;
    @Getter @Setter private WorldChallenge challenge;
    @Getter private List<DungeonSettleListener> dungeonSettleListeners;
    @Getter @Setter private int prevScene; // Id of the previous scene
    @Getter @Setter private int prevScenePoint;
    @Getter @Setter private int killedMonsterCount;
    private Set<SceneNpcBornEntry> npcBornEntrySet;
    @Getter private boolean finishedLoading = false;
    @Getter private int tickCount = 0;
    @Getter private boolean isPaused = false;

    public Scene(World world, SceneData sceneData) {
        this.world = world;
        this.sceneData = sceneData;
        this.players = new CopyOnWriteArrayList<>();
        this.entities = new ConcurrentHashMap<>();

        this.prevScene = 3;
        this.sceneRoutes = GameData.getSceneRoutes(getId());

        startWorldTime = world.getWorldTime();

        this.spawnedEntities = ConcurrentHashMap.newKeySet();
        this.deadSpawnedEntities = ConcurrentHashMap.newKeySet();
        this.loadedBlocks = ConcurrentHashMap.newKeySet();
        this.loadedGroups = ConcurrentHashMap.newKeySet();
        this.loadedGridBlocks = new HashSet<>();
        this.npcBornEntrySet = ConcurrentHashMap.newKeySet();
        this.scriptManager = new SceneScriptManager(this);
        this.unlockedForces = new HashSet<>();
    }

    public int getId() {
        return sceneData.getId();
    }

    public SceneType getSceneType() {
        return getSceneData().getSceneType();
    }

    public int getPlayerCount() {
        return this.getPlayers().size();
    }

    public GameEntity getEntityById(int id) {
        return this.entities.get(id);
    }

    public GameEntity getEntityByConfigId(int configId) {
        return this.entities.values().stream()
            .filter(x -> x.getConfigId() == configId)
            .findFirst()
            .orElse(null);
    }

    public GameEntity getEntityByConfigId(int configId, int groupId) {
        return this.entities.values().stream()
            .filter(x -> x.getConfigId() == configId && x.getGroupId() == groupId)
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public Route getSceneRouteById(int routeId) {
        return sceneRoutes.get(routeId);
    }

    public void setPaused(boolean paused) {
        if (isPaused != paused) {
            isPaused = paused;
            broadcastPacket(new PacketSceneTimeNotify(this));
        }
    }

    public int getSceneTime() {
        return (int) (getWorld().getWorldTime() - startWorldTime);
    }

    public int getSceneTimeSeconds() {
        return getSceneTime() / 1000;
    }

    public void addDungeonSettleObserver(DungeonSettleListener dungeonSettleListener) {
        if (dungeonSettleListeners == null) {
            dungeonSettleListeners = new ArrayList<>();
        }
        dungeonSettleListeners.add(dungeonSettleListener);
    }

    public void triggerDungeonEvent(DungeonPassConditionType conditionType, int... params) {
        if (dungeonManager == null) {
            return;
        }
        dungeonManager.triggerEvent(conditionType, params);
    }

    public boolean isInScene(GameEntity entity) {
        return this.entities.containsKey(entity.getId());
    }

    public synchronized void addPlayer(Player player) {
        // Check if player already in
        if (getPlayers().contains(player)) {
            return;
        }

        // Remove player from prev scene
        if (player.getScene() != null) {
            player.getScene().removePlayer(player);
        }

        // Add
        getPlayers().add(player);
        player.setSceneId(this.getId());
        player.setScene(this);
        if (player == getWorld().getOwner()) {
            player.getBlossomManager().setScene(this);
        }
        this.setupPlayerAvatars(player);
    }

    public synchronized void removePlayer(Player player) {
        // Remove from challenge if leaving
        if (this.getChallenge() != null && this.getChallenge().inProgress()) {
            player.sendPacket(new PacketDungeonChallengeFinishNotify(this.getChallenge()));
        }

        // Remove player from scene
        getPlayers().remove(player);
        player.setScene(null);

        // Remove player avatars
         this.removePlayerAvatars(player);

        // Remove player gadgets
        for (EntityBaseGadget gadget : player.getTeamManager().getGadgets()) {
            this.removeEntity(gadget);
        }

        // Deregister scene if not in use
        if (this.getPlayerCount() <= 0 && !this.dontDestroyWhenEmpty) {
            this.getScriptManager().onDestroy();
            this.getWorld().deregisterScene(this);
        }
        this.saveGroups();
    }

    private void setupPlayerAvatars(@NotNull Player player) {
        // Clear entities from old team
        List<EntityAvatar> activeTeam = player.getTeamManager().getActiveTeam();
        activeTeam.clear();
        Optional.ofNullable(player.getTeamManager().getCurrentTeamInfo())
            .ifPresent(info -> info.getAvatars().forEach(avatarId -> activeTeam.add(
                new EntityAvatar(player.getScene(), player.getAvatars().getAvatarById(avatarId))
            )));
    }

    private synchronized void removePlayerAvatars(@NotNull Player player) {
        removeEntities(player.getTeamManager().getActiveTeam(), VisionType.VISION_TYPE_MISS);
    }

    public void spawnPlayer(Player player) {
        var teamManager = player.getTeamManager();
        if (this.isInScene(teamManager.getCurrentAvatarEntity())) {
            return;
        }

        if (teamManager.getCurrentAvatarEntity().getFightProperty(FightProperty.FIGHT_PROP_CUR_HP) <= 0f) {
            teamManager.getCurrentAvatarEntity().setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, 1f);
        }

        this.addEntity(teamManager.getCurrentAvatarEntity());

        // Notify the client of any extra skill charges
        teamManager.getActiveTeam().stream().map(EntityAvatar::getAvatar).forEach(Avatar::sendSkillExtraChargeMap);
    }

    private void addEntityDirectly(GameEntity entity) {
        getEntities().put(entity.getId(), entity);
        entity.onCreate(); // Call entity create event
    }

    public synchronized void addEntity(GameEntity entity) {
        this.addEntityDirectly(entity);
        this.broadcastPacket(new PacketSceneEntityAppearNotify(entity));
    }

    public synchronized void addEntityToSingleClient(Player player, GameEntity entity) {
        this.addEntityDirectly(entity);
        player.sendPacket(new PacketSceneEntityAppearNotify(entity));

    }

    public void addEntities(Collection<? extends GameEntity> entities) {
        addEntities(entities, VisionType.VISION_TYPE_BORN);
    }

    private static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<>(
                list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    public synchronized void addEntities(Collection<? extends GameEntity> entities, VisionType visionType) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (GameEntity entity : entities) {
            this.addEntityDirectly(entity);
        }

        for(val l : chopped(new ArrayList<>(entities), 100)) {
            this.broadcastPacket(new PacketSceneEntityAppearNotify(l, visionType));
        }
    }

    private GameEntity removeEntityDirectly(GameEntity entity) {
        var removed = getEntities().remove(entity.getId());
        if (removed != null) {
            removed.onRemoved();//Call entity remove event
        }
        return removed;
    }

    public void removeEntity(GameEntity entity) {
        this.removeEntity(entity, VisionType.VISION_TYPE_DIE);
    }

    public synchronized void removeEntity(GameEntity entity, VisionType visionType) {
        GameEntity removed = this.removeEntityDirectly(entity);
        if (removed != null) {
            this.broadcastPacket(new PacketSceneEntityDisappearNotify(removed, visionType));
        }
    }

    public synchronized void removeEntities(List<? extends GameEntity> entity, VisionType visionType) {
        var toRemove = entity.stream()
            .filter(Objects::nonNull)
            .map(this::removeEntityDirectly)
            .filter(Objects::nonNull)
            .toList();
        if (toRemove.size() > 0) {
            this.broadcastPacket(new PacketSceneEntityDisappearNotify(toRemove, visionType));
        }
    }

    public synchronized void replaceEntity(EntityAvatar oldEntity, EntityAvatar newEntity) {
        this.removeEntityDirectly(oldEntity);
        this.addEntityDirectly(newEntity);
        this.broadcastPacket(new PacketSceneEntityDisappearNotify(oldEntity, VisionType.VISION_TYPE_REPLACE));
        this.broadcastPacket(new PacketSceneEntityAppearNotify(newEntity, VisionType.VISION_TYPE_REPLACE, oldEntity.getId()));
    }

    public void showOtherEntities(Player player) {
        GameEntity currentEntity = player.getTeamManager().getCurrentAvatarEntity();
        List<GameEntity> entities = this.getEntities().values().stream().filter(entity -> entity != currentEntity).toList();

        player.sendPacket(new PacketSceneEntityAppearNotify(entities, VisionType.VISION_TYPE_MEET));
    }

    public void handleAttack(AttackResult result) {
        //GameEntity attacker = getEntityById(result.getAttackerId());
        GameEntity target = getEntityById(result.getDefenseId());
        ElementType attackType = ElementType.getTypeByValue(result.getElementType());
        if (target == null) return;

        // Godmode check
        if (target instanceof EntityAvatar entityAvatar) {
            if (entityAvatar.getPlayer().inGodmode()) return;

            if (result.getDamage() != result.getDamageShield()) { // when avatar actually have a shield
                Optional.ofNullable(getChallenge()).ifPresent(c ->
                    c.onDamageMonsterOrShield(getEntityById(result.getAttackerId()), result.getDamageShield() - result.getDamage()));
            }
        }
        target.damage(result.getDamage(), result.getAttackerId(), attackType);

        if (target instanceof EntityGadget gadget) {
            Optional.ofNullable(getChallenge()).ifPresent(c -> c.onGadgetDamage(gadget));
        }
    }

    public void killEntity(GameEntity target) {
        killEntity(target, 0);
    }

    public void killEntity(GameEntity target, int attackerId) {
        GameEntity attacker = null;

        if (attackerId > 0) {
            attacker = getEntityById(attackerId);
        }

        if (attacker != null) {
            // Check codex
            if (attacker instanceof EntityClientGadget gadgetAttacker) {
                var clientGadgetOwner = getEntityById(gadgetAttacker.getOwnerEntityId());
                if (clientGadgetOwner instanceof EntityAvatar) {
                    ((EntityClientGadget) attacker).getOwner().getCodex().checkAnimal(target, CodexAnimalData.CountType.CODEX_COUNT_TYPE_KILL);
                }
            } else if (attacker instanceof EntityAvatar avatarAttacker) {
                avatarAttacker.getPlayer().getCodex().checkAnimal(target, CodexAnimalData.CountType.CODEX_COUNT_TYPE_KILL);
            }
        }

        // Packet
        this.broadcastPacket(new PacketLifeStateChangeNotify(attackerId, target, LifeState.LIFE_DEAD));

        // Reward drop
        if (target instanceof EntityMonster && this.getSceneType() != SceneType.SCENE_DUNGEON) {
            getWorld().getServer().getDropSystem().callDrop((EntityMonster) target);
        }

        // Remove entity from world
        this.removeEntity(target);

        // Death event
        target.onDeath(attackerId);
        triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_KILL_MONSTER_COUNT, ++killedMonsterCount);
    }

    public void onTick() {
        // disable script for home
        if (this.getSceneType() == SceneType.SCENE_HOME_WORLD || this.getSceneType() == SceneType.SCENE_HOME_ROOM) {
            finishLoading();
            return;
        }
        if (this.getScriptManager().isInit()) {
            //this.checkBlocks();
            if (tickCount % 2 == 0) {
                checkGroups();
            }
        } else {
            // TEMPORARY
            this.checkSpawns();
        }
        // Triggers
        this.scriptManager.checkRegions();

        if (challenge != null) {
            challenge.onCheckTimeOut();
        }

        val sceneTime = getSceneTimeSeconds();
        getEntities().forEach((eid, e) -> e.onTick(sceneTime));

        checkNpcGroup();
        finishLoading();
        checkPlayerRespawn();
        if (tickCount % 10 == 0) {
            broadcastPacket(new PacketSceneTimeNotify(this));
        }
        tickCount++;
    }

    private void checkPlayerRespawn() {
        if(getScriptManager().getConfig() == null){
            return;
        }
        val diePos = getScriptManager().getConfig().die_y;
        players.forEach(player -> {
            //Check if we need a respawn
            if (diePos >= player.getPosition().getY()) {
                //Respawn the player
                respawnPlayer(player);
            }
        });
        getEntities().forEach((eid, e) -> {
            if(diePos >= e.getPosition().getY()){
                killEntity(e);
            }
        });
    }

    public Position getDefaultLocation(Player player) {
        val defaultPosition = getScriptManager().getConfig().born_pos;
        return defaultPosition != null ? defaultPosition : player.getPosition();
    }

    private Position getDefaultRot(Player player) {
        val defaultRotation = getScriptManager().getConfig().born_rot;
        return defaultRotation != null ? defaultRotation : player.getRotation();
    }

    private Position getRespawnLocation(Player player) {
        //TODO get last valid location the player stood on
        val lastCheckpointPos = dungeonManager != null ? dungeonManager.getRespawnLocation() : null;
        return lastCheckpointPos != null ? lastCheckpointPos : getDefaultLocation(player);
    }

    private Position getRespawnRotation(Player player) {
        val lastCheckpointRot = dungeonManager != null ? dungeonManager.getRespawnRotation() : null;
        return lastCheckpointRot != null ? lastCheckpointRot : getDefaultRot(player);

    }

    public boolean respawnPlayer(Player player) {
        player.getTeamManager().onAvatarDieDamage();

        // todo should probably respawn the player at the last valid location
        val targetPos = getRespawnLocation(player);
        val targetRot = getRespawnRotation(player);
        val teleportProps = TeleportProperties.builder()
            .sceneId(getId())
            .teleportTo(targetPos)
            .teleportRot(targetRot)
            .teleportType(PlayerTeleportEvent.TeleportType.INTERNAL)
            .enterType(EnterTypeOuterClass.EnterType.ENTER_TYPE_GOTO)
            .enterReason(dungeonManager != null ? EnterReason.DungeonReviveOnWaypoint : EnterReason.Revival);


        return getWorld().transferPlayerToScene(player, teleportProps.build());
    }

    public void finishLoading() {
        if (finishedLoading) {
            return;
        }
        this.finishedLoading = true;
        afterLoadedCallbacks.forEach(Runnable::run);
        afterLoadedCallbacks.clear();
    }

    public void runWhenFinished(Runnable runnable) {
        if (isFinishedLoading()) {
            runnable.run();
            return;
        }
        afterLoadedCallbacks.add(runnable);
    }

    public int getEntityLevel(int baseLevel, int worldLevelOverride) {
        return Math.max(1, Math.min(Math.max(worldLevelOverride + baseLevel - 22, baseLevel), 100));
    }

    public void checkNpcGroup() {
        Set<SceneNpcBornEntry> npcBornEntries = ConcurrentHashMap.newKeySet();
        for (Player player : this.getPlayers()) {
            npcBornEntries.addAll(loadNpcForPlayer(player));
        }

        // clear the unreachable group for client
        var toUnload = this.npcBornEntrySet.stream()
            .filter(i -> !npcBornEntries.contains(i))
            .map(SceneNpcBornEntry::getGroupId)
            .toList();

        if (toUnload.size() > 0) {
            broadcastPacket(new PacketGroupUnloadNotify(toUnload));
            Grasscutter.getLogger().debug("Unload NPC Group {}", toUnload);
        }
        // exchange the new npcBornEntry Set
        this.npcBornEntrySet = npcBornEntries;
    }

    public synchronized void checkSpawns() {
        Set<SpawnDataEntry.GridBlockId> loadedGridBlocks = new HashSet<>();
        for (Player player : this.getPlayers()) {
            loadedGridBlocks.addAll(Arrays.asList(SpawnDataEntry.GridBlockId.getAdjacentGridBlockIds(player.getSceneId(), player.getPosition())));
        }
        if (this.loadedGridBlocks.containsAll(loadedGridBlocks)) {  // Don't recalculate static spawns if nothing has changed
            return;
        }
        this.loadedGridBlocks = loadedGridBlocks;
        var spawnLists = GameDepot.getSpawnLists();
        Set<SpawnDataEntry> visible = new HashSet<>();
        for (var block : loadedGridBlocks) {
            var spawns = spawnLists.get(block);
            if (spawns != null) {
                visible.addAll(spawns);
            }
        }

        // World level
        WorldLevelData worldLevelData = GameData.getWorldLevelDataMap().get(getWorld().getWorldLevel());
        int worldLevelOverride = 0;

        if (worldLevelData != null) {
            worldLevelOverride = worldLevelData.getMonsterLevel();
        }

        // Todo
        List<GameEntity> toAdd = new ArrayList<>();
        List<GameEntity> toRemove = new ArrayList<>();
        var spawnedEntities = this.getSpawnedEntities();
        for (SpawnDataEntry entry : visible) {
            // If spawn entry is in our view and hasn't been spawned/killed yet, we should spawn it
            if (!spawnedEntities.contains(entry) && !this.getDeadSpawnedEntities().contains(entry)) {
                // Entity object holder
                GameEntity entity = null;

                // Check if spawn entry is monster or gadget
                if (entry.getMonsterId() > 0) {
                    MonsterData data = GameData.getMonsterDataMap().get(entry.getMonsterId());
                    if (data == null) continue;

                    int level = this.getEntityLevel(entry.getLevel(), worldLevelOverride);

                    EntityMonster monster = new EntityMonster(this, data, entry.getPos(), level);
                    monster.getRotation().set(entry.getRot());
                    monster.setGroupId(entry.getGroup().getGroupId());
                    monster.setPoseId(entry.getPoseId());
                    monster.setConfigId(entry.getConfigId());
                    monster.setSpawnEntry(entry);

                    entity = monster;
                } else if (entry.getGadgetId() > 0) {
                    EntityGadget gadget = new EntityGadget(this, entry.getGadgetId(), entry.getPos(), entry.getRot());
                    gadget.setGroupId(entry.getGroup().getGroupId());
                    gadget.setConfigId(entry.getConfigId());
                    gadget.setSpawnEntry(entry);
                    int state = entry.getGadgetState();
                    if (state > 0) {
                        gadget.setState(state);
                    }
                    gadget.buildContent();

                    gadget.setFightProperty(FightProperty.FIGHT_PROP_BASE_HP, Float.POSITIVE_INFINITY);
                    gadget.setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, Float.POSITIVE_INFINITY);
                    gadget.setFightProperty(FightProperty.FIGHT_PROP_MAX_HP, Float.POSITIVE_INFINITY);

                    entity = gadget;
                }

                if (entity == null) continue;

                // Add to scene and spawned list
                toAdd.add(entity);
                spawnedEntities.add(entry);
            }
        }

        for (GameEntity entity : this.getEntities().values()) {
            var spawnEntry = entity.getSpawnEntry();
            if (spawnEntry != null && !visible.contains(spawnEntry)) {
                toRemove.add(entity);
                spawnedEntities.remove(spawnEntry);
            }
        }

        if (toAdd.size() > 0) {
            toAdd.forEach(this::addEntityDirectly);
            this.broadcastPacket(new PacketSceneEntityAppearNotify(toAdd, VisionType.VISION_TYPE_BORN));
        }
        if (toRemove.size() > 0) {
            toRemove.forEach(this::removeEntityDirectly);
            this.broadcastPacket(new PacketSceneEntityDisappearNotify(toRemove, VisionType.VISION_TYPE_REMOVE));
        }
    }

    public List<SceneBlock> getPlayerActiveBlocks(Player player) {
        // consider the borders' entities of blocks, so we check if contains by index
        return SceneIndexManager.queryNeighbors(getScriptManager().getBlocksIndex(),
            player.getPosition().toXZDoubleArray(), Grasscutter.getConfig().server.game.loadEntitiesForPlayerRange);
    }

    public Set<Integer> getPlayerActiveGroups(Player player) {
        // consider the borders' entities of blocks, so we check if contains by index
        Position playerPosition = player.getPosition();
        Set<Integer> activeGroups = new HashSet<>();
        for(int i = 0; i < 4; i++) {
            Grid grid = getScriptManager().getGroupGrids().get(i);

            activeGroups.addAll(grid.getNearbyGroups(i, playerPosition));
        }

        return activeGroups;
    }

    public synchronized boolean loadBlock(SceneBlock block) {
        if (this.loadedBlocks.contains(block)) return false;
        this.onLoadBlock(block, this.players);
        this.loadedBlocks.add(block);
        return true;
    }

    public synchronized void checkGroups() {
        var playerMoved = this.players.stream().filter(p -> p.getLastCheckedPosition() == null || !p.getLastCheckedPosition().equal2d(p.getPosition())).toList();
        if(playerMoved.isEmpty()) return;

        playerMoved.forEach(p -> p.setLastCheckedPosition(p.getPosition().clone()));

        Set<Integer> visible = this.players.stream()
            .map(this::getPlayerActiveGroups)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        for (SceneGroup group : this.loadedGroups) {
            if (!visible.contains(group.id) && !group.dynamic_load)
                unloadGroup(scriptManager.getBlocks().get(group.block_id), group.id);
        }

        List<SceneGroup> toLoad = visible.stream().filter(g -> this.loadedGroups.stream().noneMatch(gr -> gr.id == g)).map(g -> {
            for(var b : scriptManager.getBlocks().values()) {
                loadBlock(b);
                SceneGroup group = b.groups.getOrDefault(g, null);
                if(group != null && !group.dynamic_load) return group;
            }

            return null;
        }).filter(Objects::nonNull).toList();

        this.onLoadGroup(toLoad);
        if(!toLoad.isEmpty()) this.onRegisterGroups();
    }

    public void onLoadBlock(SceneBlock block, List<Player> players) {
        this.getScriptManager().loadBlockFromScript(block);
        scriptManager.getLoadedGroupSetPerBlock().put(block.id, new HashSet<>());

        Grasscutter.getLogger().info("Scene {} Block {} loaded.", this.getId(), block.id);
    }

    public int loadDynamicGroup(int group_id) {
        SceneGroup group = getScriptManager().getGroupById(group_id);
        if(group == null || getScriptManager().getGroupInstanceById(group_id) != null) return -1; //Group not found or already instanced

        onLoadGroup(new ArrayList<>(List.of(group)));

        if(GameData.getGroupReplacements().containsKey(group_id)) onRegisterGroups();

        if (group.init_config == null) return -1;
        return group.init_config.suite;
    }

    public boolean unregisterDynamicGroup(int groupId){
        SceneGroup group = getScriptManager().getGroupById(groupId);
        if(group == null) return false;

        SceneBlock block = getScriptManager().getBlocks().get(group.block_id);
        unloadGroup(block, groupId);
        return true;
    }

    public void onRegisterGroups() {
        Set<SceneGroup> sceneGroups = this.loadedGroups;
        Map<Integer, SceneGroup> sceneGroupMap = sceneGroups.stream().collect(Collectors.toMap(item -> item.id, item -> item));
        List<Integer> sceneGroupsIds = sceneGroups.stream()
            .map(group -> group.id)
            .toList();
        List<Integer> dynamicGroups = sceneGroups.stream()
            .filter(group -> group.dynamic_load)
            .map(group -> group.id)
            .toList();

        //Create the graph
        List<KahnsSort.Node> nodes = new ArrayList<>();
        List<Integer> groupList = new ArrayList<>();
        GameData.getGroupReplacements().values().stream()
            .filter(replacement -> dynamicGroups.contains(replacement.id))
            .forEach(replacement -> {
                Grasscutter.getLogger().info("Graph ordering replacement {}", replacement);
                replacement.replace_groups.forEach(group -> {
                    nodes.add(new KahnsSort.Node(replacement.id, group));
                    if(!groupList.contains(group)) groupList.add(group);
                });
                if(!groupList.contains(replacement.id)) groupList.add(replacement.id);
            });

        List<Integer> dynamicGroupsOrdered = KahnsSort.doSort(new KahnsSort.Graph(nodes, groupList));

        //Now we can start unloading and loading groups :D
        Optional.ofNullable(dynamicGroupsOrdered).ifPresent(gList -> gList.forEach(group -> {
            //isGroupJoinReplacement
            if(!GameData.getGroupReplacements().containsKey((int) group)) return;

            GroupReplacementData data = GameData.getGroupReplacements().get((int) group);

            getLoadedGroups().stream().filter(g -> g.id == group).findFirst().ifPresent(sceneGroupReplacement -> {
                if(sceneGroupReplacement.is_replaceable == null) return;

                data.replace_groups.iterator().forEachRemaining(replace_group -> {
                    if(!sceneGroupsIds.contains(replace_group)) return;

                    //Check if we can replace this group
                    SceneGroup sceneGroup = sceneGroupMap.get(replace_group);
                    if(sceneGroup != null && sceneGroup.is_replaceable != null
                        && ((sceneGroup.is_replaceable.value && sceneGroup.is_replaceable.version
                            <= sceneGroupReplacement.is_replaceable.version)
                        || sceneGroup.is_replaceable.new_bin_only)) {
                        unloadGroup(scriptManager.getBlocks().get(sceneGroup.block_id), replace_group);
                        Grasscutter.getLogger().info("Graph ordering: unloaded {}", replace_group);
                    }
                });
            });
        }));
    }

    public void loadTriggerFromGroup(SceneGroup group, String triggerName) {
        //Load triggers and regions
        getScriptManager().registerTrigger(group.triggers.values().stream().filter(p -> p.getName().contains(triggerName)).toList());
        group.regions.values().stream().filter(q -> q.config_id == Integer.parseInt(triggerName.substring(13))).map(region -> new EntityRegion(this, region))
            .forEach(getScriptManager()::registerRegion);
    }

    public void onLoadGroup(List<SceneGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        for (SceneGroup group : groups) {
            if(this.loadedGroups.contains(group)) continue;

            // We load the script files for the groups here
            this.getScriptManager().loadGroupFromScript(group);
            if(!scriptManager.getLoadedGroupSetPerBlock().containsKey(group.block_id)) onLoadBlock(scriptManager.getBlocks().get(group.block_id), players);
            scriptManager.getLoadedGroupSetPerBlock().get(group.block_id).add(group);
        }

        // Spawn gadgets AFTER triggers are added
        // TODO
        var entities = new ArrayList<GameEntity>();
        var entitiesBorn = new ArrayList<GameEntity>();
        for (SceneGroup group : groups) {
            if(this.loadedGroups.contains(group)) continue;

            if (group.init_config == null) {
                continue;
            }

            SceneGroupInstance groupInstance = this.getScriptManager().getGroupInstanceById(group.id);
            var cachedInstance = this.getScriptManager().getCachedGroupInstanceById(group.id);
            if(cachedInstance != null) {
                cachedInstance.setLuaGroup(group);
                groupInstance = cachedInstance;
            }

            // Load garbages
            /*List<SceneGadget> garbageGadgets = group.getGarbageGadgets();

            if (garbageGadgets != null) {
                entities.addAll(garbageGadgets.stream().map(g -> scriptManager.createGadget(group.id, group.block_id, g))
                    .filter(Objects::nonNull)
                    .toList());
            }*/

            // Load suites
            //int suite = group.findInitSuiteIndex(0);
            this.getScriptManager().refreshGroup(groupInstance, 0, false, entitiesBorn); //This is what the official server does

            this.loadedGroups.add(group);
        }

        scriptManager.meetEntities(entities);
        scriptManager.addEntities(entitiesBorn);
        groups.forEach(g -> scriptManager.callEvent(new ScriptArgs(g.id, EventType.EVENT_GROUP_LOAD, g.id)));
        Grasscutter.getLogger().info("Scene {} loaded {} group(s)", this.getId(), groups.size());
    }

    public void unloadGroup(SceneBlock block, int group_id) {
        List<GameEntity> toRemove = this.getEntities().values().stream()
                .filter(e -> e != null && (e.getBlockId() == block.id && e.getGroupId() == group_id)).toList();

        if (toRemove.size() > 0) {
            toRemove.forEach(this::removeEntityDirectly);
            this.broadcastPacket(new PacketSceneEntityDisappearNotify(toRemove, VisionType.VISION_TYPE_REMOVE));
        }

        SceneGroup group = block.groups.get(group_id);
        if (group.triggers != null) {
            group.triggers.values().forEach(getScriptManager()::deregisterTrigger);
        }
        if (group.regions != null) {
            group.regions.values().forEach(getScriptManager()::deregisterRegion);
        }

        if(scriptManager.getLoadedGroupSetPerBlock().containsKey(block.id))
            scriptManager.getLoadedGroupSetPerBlock().get(block.id).remove(group);
        this.loadedGroups.remove(group);

        if(scriptManager.getLoadedGroupSetPerBlock().get(block.id).isEmpty()) {
            scriptManager.getLoadedGroupSetPerBlock().remove(block.id);
            Grasscutter.getLogger().info("Scene {} Block {} is unloaded.", this.getId(), block.id);
        }

        broadcastPacket(new PacketGroupUnloadNotify(List.of(group_id)));
        scriptManager.unregisterGroup(group);
    }

    // Gadgets

    public void onPlayerCreateGadget(EntityClientGadget gadget) {
        // Directly add
        this.addEntityDirectly(gadget);

        // Add to owner's gadget list
        gadget.getOwner().getTeamManager().getGadgets().add(gadget);

        // Optimization
        if (this.getPlayerCount() == 1 && this.getPlayers().get(0) == gadget.getOwner()) {
            return;
        }

        this.broadcastPacketToOthers(gadget.getOwner(), new PacketSceneEntityAppearNotify(gadget));
    }

    public void onPlayerDestroyGadget(int entityId) {
        GameEntity entity = getEntities().get(entityId);

        if (!(entity instanceof EntityClientGadget gadget)) {
            return;
        }

        // Get and remove entity
        this.removeEntityDirectly(gadget);

        // Remove from owner's gadget list
        gadget.getOwner().getTeamManager().getGadgets().remove(gadget);

        // Optimization
        if (this.getPlayerCount() == 1 && this.getPlayers().get(0) == gadget.getOwner()) {
            return;
        }

        this.broadcastPacketToOthers(gadget.getOwner(), new PacketSceneEntityDisappearNotify(gadget, VisionType.VISION_TYPE_DIE));
    }

    // Broadcasting

    public void broadcastPacket(BasePacket packet) {
        // Send to all players - might have to check if player has been sent data packets
        for (Player player : this.getPlayers()) {
            player.getSession().send(packet);
        }
    }

    public void broadcastPacketToOthers(Player excludedPlayer, BasePacket packet) {
        // Optimization
        if (this.getPlayerCount() == 1 && this.getPlayers().get(0) == excludedPlayer) {
            return;
        }
        // Send to all players - might have to check if player has been sent data packets
        for (Player player : this.getPlayers()) {
            if (player == excludedPlayer) {
                continue;
            }
            // Send
            player.getSession().send(packet);
        }
    }

    public void addItemEntity(int itemId, int amount, GameEntity bornForm) {
        ItemData itemData = GameData.getItemDataMap().get(itemId);
        if (itemData == null) {
            return;
        }
        if (itemData.isEquip()) {
            float range = (1.5f + (.05f * amount));
            for (int i = 0; i < amount; i++) {
                Position pos = bornForm.getPosition().nearby2d(range).addZ(.9f);  // Why Z?
                EntityItem entity = new EntityItem(this, null, itemData, pos, 1);
                addEntity(entity);
            }
        } else {
            EntityItem entity = new EntityItem(this, null, itemData, bornForm.getPosition().clone().addZ(.9f), amount);  // Why Z?
            addEntity(entity);
        }
    }

    public void loadNpcForPlayerEnter(Player player) {
        this.npcBornEntrySet.addAll(loadNpcForPlayer(player));
    }

    private List<SceneNpcBornEntry> loadNpcForPlayer(Player player) {
        var pos = player.getPosition();
        var data = GameData.getSceneNpcBornData().get(getId());
        if (data == null) {
            return List.of();
        }

        var npcList = SceneIndexManager.queryNeighbors(data.getIndex(), pos.toDoubleArray(),
            Grasscutter.getConfig().server.game.loadEntitiesForPlayerRange);

        var sceneNpcBornEntries = npcList.stream()
            .filter(i -> !this.npcBornEntrySet.contains(i))
            .toList();

        if (sceneNpcBornEntries.size() > 0) {
            this.broadcastPacket(new PacketGroupSuiteNotify(sceneNpcBornEntries));
            Grasscutter.getLogger().debug("Loaded Npc Group Suite {}", sceneNpcBornEntries);
        }
        return npcList;
    }

    public void loadGroupForQuest(List<QuestGroupSuite> sceneGroupSuite) {
        if (!scriptManager.isInit()) {
            return;
        }

        sceneGroupSuite.forEach(i -> {
            var group = scriptManager.getGroupById(i.getGroup());
            if (group == null) {
                return;
            }
            var groupInstance = scriptManager.getGroupInstanceById(i.getGroup());
            var suite = group.getSuiteByIndex(i.getSuite());
            if (suite == null || groupInstance == null) {
                return;
            }
            scriptManager.refreshGroup(groupInstance, i.getSuite(), false);
        });
    }

    public void unlockForce(int force) {
        unlockedForces.add(force);
        broadcastPacket(new PacketSceneForceUnlockNotify(force, true));
    }

    public void lockForce(int force) {
        unlockedForces.remove(force);
        broadcastPacket(new PacketSceneForceLockNotify(force));
    }

    public void selectWorktopOptionWith(SelectWorktopOptionReqOuterClass.SelectWorktopOptionReq req) {
        GameEntity entity = getEntityById(req.getGadgetEntityId());
        if (entity == null) {
            return;
        }
        // Handle
        if (entity instanceof EntityGadget gadget) {
            if (gadget.getContent() instanceof GadgetWorktop worktop) {
                boolean shouldDelete = worktop.onSelectWorktopOption(req);
                if (shouldDelete) {
                    entity.getScene().removeEntity(entity, VisionType.VISION_TYPE_REMOVE);
                }
            }
        }
    }

    public void saveGroups() {
        this.getScriptManager().getCachedGroupInstances().values().forEach(SceneGroupInstance::save);
    }
}
