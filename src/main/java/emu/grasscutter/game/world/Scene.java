package emu.grasscutter.game.world;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameDepot;
import emu.grasscutter.data.binout.SceneNpcBornEntry;
import emu.grasscutter.data.binout.routes.Route;
import emu.grasscutter.data.excels.*;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.DungeonSettleListener;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.dungeons.seal_battle.SealBattle;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.entity.gadget.GadgetWorktop;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.TeamInfo;
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
import java.util.stream.IntStream;


public class Scene {
    @Getter private final World world;
    @Getter private final SceneData sceneData;
    @Getter private final List<Player> players = new CopyOnWriteArrayList<>();
    @Getter private final Map<Integer, GameEntity> entities = new ConcurrentHashMap<>();
    @Getter private final Set<SpawnDataEntry> spawnedEntities = ConcurrentHashMap.newKeySet();
    @Getter private final Set<SpawnDataEntry> deadSpawnedEntities = ConcurrentHashMap.newKeySet();
    @Getter private final Set<SceneBlock> loadedBlocks = ConcurrentHashMap.newKeySet();
    @Getter private final Set<SceneGroup> loadedGroups = ConcurrentHashMap.newKeySet();
    @Getter private final Set<Integer> replacedGroup = ConcurrentHashMap.newKeySet();
    private final HashSet<Integer> unlockedForces = new HashSet<>();
    private final List<Runnable> afterLoadedCallbacks = new ArrayList<>();
    private final long startWorldTime;
    @Getter @Setter DungeonManager dungeonManager;
    @Getter Int2ObjectMap<Route> sceneRoutes;
    private Set<SpawnDataEntry.GridBlockId> loadedGridBlocks = new HashSet<>();
    @Getter @Setter private boolean dontDestroyWhenEmpty;
    @Getter private final SceneScriptManager scriptManager;
    @Getter @Setter private WorldChallenge challenge;
    @Getter @Setter private SealBattle sealBattle;
    @Getter private List<DungeonSettleListener> dungeonSettleListeners;
    @Getter @Setter private int prevScene; // Id of the previous scene
    @Getter @Setter private int prevScenePoint;
    @Getter @Setter private int killedMonsterCount;
    private Set<SceneNpcBornEntry> npcBornEntrySet = ConcurrentHashMap.newKeySet();
    @Getter private boolean finishedLoading = false;
    @Getter private int tickCount = 0;
    @Getter private boolean isPaused = false;

    public Scene(World world, SceneData sceneData) {
        this.world = world;
        this.sceneData = sceneData;

        this.prevScene = 3;
        this.sceneRoutes = GameData.getSceneRoutes(getId());

        this.startWorldTime = world.getWorldTime();
        this.scriptManager = new SceneScriptManager(this);
    }

    public int getId() {
        return getSceneData().getId();
    }

    public SceneType getSceneType() {
        return getSceneData().getSceneType();
    }

    public int getPlayerCount() {
        return getPlayers().size();
    }

    public GameEntity getEntityById(int id) {
        return getEntities().get(id);
    }

    public GameEntity getEntityByConfigId(int configId) {
        return getEntities().values().stream()
            .filter(x -> x.getConfigId() == configId)
            .findFirst()
            .orElse(null);
    }

    public GameEntity getEntityByConfigId(int configId, int groupId) {
        return getEntities().values().stream()
            .filter(x -> x.getConfigId() == configId && x.getGroupId() == groupId)
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public Route getSceneRouteById(int routeId) {
        return getSceneRoutes().get(routeId);
    }

    public void setPaused(boolean paused) {
        if (this.isPaused != paused) {
            this.isPaused = paused;
            broadcastPacket(new PacketSceneTimeNotify(this));
        }
    }

    public int getSceneTime() {
        return (int) (getWorld().getWorldTime() - this.startWorldTime);
    }

    public int getSceneTimeSeconds() {
        return getSceneTime() / 1000;
    }

    public void addDungeonSettleObserver(DungeonSettleListener dungeonSettleListener) {
        if (this.dungeonSettleListeners == null) {
            this.dungeonSettleListeners = new ArrayList<>();
        }
        getDungeonSettleListeners().add(dungeonSettleListener);
    }

    public void triggerDungeonEvent(DungeonPassConditionType conditionType, int... params) {
        Optional.ofNullable(getDungeonManager()).ifPresent(m -> m.triggerEvent(conditionType, params));
    }

    public boolean isInScene(GameEntity entity) {
        return getEntities().containsKey(entity.getId());
    }

    public synchronized void addPlayer(Player player) {
        // Check if player already in
        if (getPlayers().contains(player)) return;

        // Remove player from prev scene
        Optional.ofNullable(player.getScene()).ifPresent(oldScene -> oldScene.removePlayer(player));

        // Add
        getPlayers().add(player);
        player.setSceneId(getId());
        player.setScene(this);
        if (player == getWorld().getOwner()) {
            player.getBlossomManager().setScene(this);
        }
        setupPlayerAvatars(player);
    }

    public synchronized void removePlayer(Player player) {
        // Remove from challenge if leaving
        Optional.ofNullable(getChallenge()).filter(WorldChallenge::inProgress)
            .ifPresent(c -> player.sendPacket(new PacketDungeonChallengeFinishNotify(c)));

        // Remove player from scene
        getPlayers().remove(player);
        player.setScene(null);

        // Remove player avatars
         removePlayerAvatars(player);

        // Remove player gadgets
        player.getTeamManager().getGadgets().forEach(this::removeEntity);

        // Deregister scene if not in use
        if (getPlayerCount() <= 0 && !this.dontDestroyWhenEmpty) {
            getScriptManager().onDestroy();
            getWorld().deregisterScene(this);
        }
        saveGroups();
    }

    private void setupPlayerAvatars(@NotNull Player player) {
        // Clear entities from old team
        player.getTeamManager().getActiveTeam().clear();
        Optional.ofNullable(player.getTeamManager().getCurrentTeamInfo())
            .map(TeamInfo::getAvatars).stream().flatMap(List::stream)
            .map(player.getAvatars()::getAvatarById).map(avatar -> new EntityAvatar(this, avatar))
            .forEach(player.getTeamManager().getActiveTeam()::add);
    }

    private synchronized void removePlayerAvatars(@NotNull Player player) {
        removeEntities(player.getTeamManager().getActiveTeam(), VisionType.VISION_TYPE_MISS);
    }

    public void spawnPlayer(Player player) {
        val teamManager = player.getTeamManager();
        if (isInScene(teamManager.getCurrentAvatarEntity())) return;

        if (teamManager.getCurrentAvatarEntity().getFightProperty(FightProperty.FIGHT_PROP_CUR_HP) <= 0f) {
            teamManager.getCurrentAvatarEntity().setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, 1f);
        }

        addEntity(teamManager.getCurrentAvatarEntity());

        // Notify the client of any extra skill charges
        teamManager.getActiveTeam().stream().map(EntityAvatar::getAvatar).forEach(Avatar::sendSkillExtraChargeMap);
    }

    private void addEntityDirectly(GameEntity entity) {
        getEntities().put(entity.getId(), entity);
        entity.onCreate(); // Call entity create event
    }

    public synchronized void addEntity(GameEntity entity) {
        addEntityDirectly(entity);
        broadcastPacket(new PacketSceneEntityAppearNotify(entity));
    }

    public synchronized void addEntityToSingleClient(Player player, GameEntity entity) {
        addEntityDirectly(entity);
        player.sendPacket(new PacketSceneEntityAppearNotify(entity));
    }

    public void addEntities(Collection<? extends GameEntity> entities) {
        addEntities(entities, VisionType.VISION_TYPE_BORN);
    }

    private static <T> List<List<T>> chopped(List<T> list, final int L) {
        return IntStream.range(0, (list.size() + L - 1) / L)
            .mapToObj(i -> list.subList(i * L, Math.min(list.size(), (i + 1) * L)))
            .toList();
    }

    public synchronized void addEntities(Collection<? extends GameEntity> entities, VisionType visionType) {
        Optional.ofNullable(entities).filter(ets -> !ets.isEmpty()).stream()
            .peek(ets -> ets.forEach(this::addEntityDirectly))
            .map(ets -> chopped(ets.stream().toList(), 100))
            .forEach(c -> c.forEach(l -> broadcastPacket(new PacketSceneEntityAppearNotify(l, visionType))));
    }

    private GameEntity removeEntityDirectly(GameEntity entity) {
        return Optional.ofNullable(getEntities().remove(entity.getId())).stream()
            .peek(GameEntity::onRemoved).findFirst().orElse(null);
    }

    public void removeEntity(GameEntity entity) {
        removeEntity(entity, VisionType.VISION_TYPE_DIE);
    }

    public synchronized void removeEntity(GameEntity entity, VisionType visionType) {
        Optional.ofNullable(removeEntityDirectly(entity))
            .ifPresent(removed -> broadcastPacket(new PacketSceneEntityDisappearNotify(removed, visionType)));
    }

    public synchronized void removeEntities(Collection<? extends GameEntity> entities, VisionType visionType) {
        Optional.of(entities.stream()
                .filter(Objects::nonNull)
                .map(this::removeEntityDirectly)
                .filter(Objects::nonNull)
                .toList())
            .filter(toRemove -> !toRemove.isEmpty())
            .ifPresent(toRemove -> broadcastPacket(new PacketSceneEntityDisappearNotify(toRemove, visionType)));
    }

    public synchronized void replaceEntity(EntityAvatar oldEntity, EntityAvatar newEntity) {
        removeEntityDirectly(oldEntity);
        addEntityDirectly(newEntity);
        broadcastPacket(new PacketSceneEntityDisappearNotify(oldEntity, VisionType.VISION_TYPE_REPLACE));
        broadcastPacket(new PacketSceneEntityAppearNotify(newEntity, VisionType.VISION_TYPE_REPLACE, oldEntity.getId()));
    }

    public void showOtherEntities(Player player) {
        GameEntity currentEntity = player.getTeamManager().getCurrentAvatarEntity();
        List<GameEntity> entities = getEntities().values().stream().filter(entity -> entity != currentEntity).toList();
        player.sendPacket(new PacketSceneEntityAppearNotify(entities, VisionType.VISION_TYPE_MEET));
    }

    public void handleAttack(AttackResult result) {
        //GameEntity attacker = getEntityById(result.getAttackerId());
        GameEntity target = getEntityById(result.getDefenseId());
        if (target == null) return;

        // Godmode check
        if (target instanceof EntityAvatar entityAvatar) {
            if (entityAvatar.getPlayer().inGodmode()) return;

            if (result.getDamage() != result.getDamageShield()) { // when avatar actually have a shield
                Optional.ofNullable(getChallenge()).ifPresent(c -> c.onDamageMonsterOrShield(
                    getEntityById(result.getAttackerId()), result.getDamageShield() - result.getDamage()));
            }
        }
        target.damage(result.getDamage(), result.getAttackerId(), ElementType.getTypeByValue(result.getElementType()));

        if (target instanceof EntityGadget gadget) {
            Optional.ofNullable(getChallenge()).ifPresent(c -> c.onGadgetDamage(gadget));
        }
    }

    public void killEntity(GameEntity target) {
        killEntity(target, 0);
    }

    public void killEntity(GameEntity target, int attackerId) {
        GameEntity attacker = getEntityById(attackerId);

        // Check codex
        if (attacker instanceof EntityClientGadget gadgetAttacker) {
            GameEntity clientGadgetOwner = getEntityById(gadgetAttacker.getOwnerEntityId());
            if (clientGadgetOwner instanceof EntityAvatar) {
                gadgetAttacker.getOwner().getCodex().checkAnimal(target, CodexAnimalData.CountType.CODEX_COUNT_TYPE_KILL);
            }
        } else if (attacker instanceof EntityAvatar avatarAttacker) {
            avatarAttacker.getPlayer().getCodex().checkAnimal(target, CodexAnimalData.CountType.CODEX_COUNT_TYPE_KILL);
        }

        // Packet
        broadcastPacket(new PacketLifeStateChangeNotify(attackerId, target, LifeState.LIFE_DEAD));

        // Reward drop
        if (target instanceof EntityMonster monster && getSceneType() != SceneType.SCENE_DUNGEON) {
            getWorld().getServer().getDropSystem().callDrop(monster);
        }

        // Remove entity from world
        removeEntity(target);

        // Death event
        target.onDeath(attackerId);
        triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_KILL_MONSTER_COUNT, ++this.killedMonsterCount);
    }

    public void onTick() {
        // disable script for home
        if (getSceneType() == SceneType.SCENE_HOME_WORLD || getSceneType() == SceneType.SCENE_HOME_ROOM) {
            finishLoading();
            return;
        }

        if (getScriptManager().isInit()) {
            //this.checkBlocks();
            if (getTickCount() % 2 == 0) {
                checkGroups();
            }
        } else {
            // TEMPORARY
            checkSpawns();
        }

        // Triggers
        getScriptManager().checkRegions();

        // check ongoing challenge
        Optional.ofNullable(getChallenge()).ifPresent(WorldChallenge::onCheckTimeOut);

        // check ongoing seal battle
        Optional.ofNullable(getSealBattle()).ifPresent(SealBattle::onCheckTimeout);

        getEntities().values().forEach(e -> e.onTick(getSceneTimeSeconds()));

        checkNpcGroup();
        finishLoading();
        checkPlayerRespawn();
        if (getTickCount() % 10 == 0) {
            broadcastPacket(new PacketSceneTimeNotify(this));
        }
        this.tickCount++;
    }

    private void checkPlayerRespawn() {
        Optional.ofNullable(getScriptManager().getConfig()).map(c -> c.die_y)
            .ifPresent(diePos -> {
                getPlayers().stream().filter(player -> diePos >= player.getPosition().getY()).forEach(this::respawnPlayer);
                getEntities().values().stream().filter(e -> diePos >= e.getPosition().getY()).forEach(this::killEntity);
            });
    }

    public Position getDefaultLocation(Player player) {
        return Optional.ofNullable(getScriptManager().getConfig().born_pos).orElse(player.getPosition());
    }

    private Position getDefaultRot(Player player) {
        return Optional.ofNullable(getScriptManager().getConfig().born_rot).orElse(player.getRotation());
    }

    private Position getRespawnLocation(Player player) {
        //TODO get last valid location the player stood on
        return Optional.ofNullable(getDungeonManager())
            .map(DungeonManager::getRespawnLocation).orElse(getDefaultLocation(player));
    }

    private Position getRespawnRotation(Player player) {
        return Optional.ofNullable(getDungeonManager())
            .map(DungeonManager::getRespawnRotation).orElse(getDefaultRot(player));
    }

    public boolean respawnPlayer(Player player) {
        player.getTeamManager().onAvatarDieDamage();

        // todo should probably respawn the player at the last valid location
        return getWorld().transferPlayerToScene(player,
            TeleportProperties.builder()
                .sceneId(getId())
                .teleportTo(getRespawnLocation(player))
                .teleportRot(getRespawnRotation(player))
                .teleportType(PlayerTeleportEvent.TeleportType.INTERNAL)
                .enterType(EnterTypeOuterClass.EnterType.ENTER_TYPE_GOTO)
                .enterReason(getDungeonManager() != null ? EnterReason.DungeonReviveOnWaypoint : EnterReason.Revival)
                .build());
    }

    public void finishLoading() {
        if (isFinishedLoading()) return;

        this.finishedLoading = true;
        this.afterLoadedCallbacks.forEach(Runnable::run);
        this.afterLoadedCallbacks.clear();
    }

    public void runWhenFinished(Runnable runnable) {
        if (isFinishedLoading()) {
            runnable.run();
            return;
        }
        this.afterLoadedCallbacks.add(runnable);
    }

    public int getEntityLevel(int baseLevel, int worldLevelOverride) {
        return Math.max(1, Math.min(Math.max(worldLevelOverride + baseLevel - 22, baseLevel), 100));
    }

    public void checkNpcGroup() {
        Set<SceneNpcBornEntry> npcBornEntries = getPlayers().stream()
            .map(this::loadNpcForPlayer).flatMap(List::stream).collect(Collectors.toSet());

        // clear the unreachable group for client
        Optional.of(this.npcBornEntrySet.stream()
                .filter(i -> !npcBornEntries.contains(i))
                .map(SceneNpcBornEntry::getGroupId)
                .toList()).stream()
            .filter(toUnload -> !toUnload.isEmpty())
            .peek(toUnload -> Grasscutter.getLogger().debug("Unload NPC Group {}", toUnload))
            .forEach(toUnload -> broadcastPacket(new PacketGroupUnloadNotify(toUnload)));

        // exchange the new npcBornEntry Set
        this.npcBornEntrySet = npcBornEntries;
    }

    public synchronized void checkSpawns() {
        Set<SpawnDataEntry.GridBlockId> loadedGridBlocks = getPlayers().stream()
            .map(p -> SpawnDataEntry.GridBlockId.getAdjacentGridBlockIds(p.getSceneId(), p.getPosition()))
            .map(Arrays::asList)
            .flatMap(List::stream)
            .collect(Collectors.toSet());

        // Don't recalculate static spawns if nothing has changed
        if (this.loadedGridBlocks.containsAll(loadedGridBlocks)) return;

        this.loadedGridBlocks = loadedGridBlocks;
        Set<SpawnDataEntry> visible = loadedGridBlocks.stream()
            .map(GameDepot.getSpawnLists()::get)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toSet());

        // World level
        int worldLevelOverride = Optional.ofNullable(GameData.getWorldLevelDataMap().get(getWorld().getWorldLevel()))
            .map(WorldLevelData::getMonsterLevel).orElse(0);

        // Todo
        List<GameEntity> toAdd = new ArrayList<>();
        List<GameEntity> toRemove = new ArrayList<>();
        Set<SpawnDataEntry> spawnedEntities = getSpawnedEntities();
        for (SpawnDataEntry entry : visible) {
            // If spawn entry is in our view and hasn't been spawned/killed yet, we should spawn it
            if (spawnedEntities.contains(entry) || getDeadSpawnedEntities().contains(entry)) continue;

            // Entity object holder
            GameEntity entity = null;

            // Check if spawn entry is monster or gadget
            if (entry.getMonsterId() > 0) {
                MonsterData data = GameData.getMonsterDataMap().get(entry.getMonsterId());
                if (data == null) continue;

                int level = getEntityLevel(entry.getLevel(), worldLevelOverride);

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
                gadget.setState(Math.max(0, entry.getGadgetState()));
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

        getEntities().values().stream()
            .filter(entity -> entity.getSpawnEntry() != null && !visible.contains(entity.getSpawnEntry()))
            .peek(toRemove::add)
            .map(GameEntity::getSpawnEntry)
            .forEach(spawnedEntities::remove);

        if (!toAdd.isEmpty()) {
            toAdd.forEach(this::addEntityDirectly);
            broadcastPacket(new PacketSceneEntityAppearNotify(toAdd, VisionType.VISION_TYPE_BORN));
        }
        if (!toRemove.isEmpty()) {
            toRemove.forEach(this::removeEntityDirectly);
            broadcastPacket(new PacketSceneEntityDisappearNotify(toRemove, VisionType.VISION_TYPE_REMOVE));
        }
    }

    public List<SceneBlock> getPlayerActiveBlocks(Player player) {
        // consider the borders' entities of blocks, so we check if contains by index
        return SceneIndexManager.queryNeighbors(getScriptManager().getBlocksIndex(),
            player.getPosition().toXZDoubleArray(), Grasscutter.getConfig().server.game.loadEntitiesForPlayerRange);
    }

    public Set<Integer> getPlayerActiveGroups(Player player) {
        // consider the borders' entities of blocks, so we check if contains by index
        return IntStream.range(0, 4)
            .mapToObj(i -> getScriptManager().getGroupGrids().get(i).getNearbyGroups(i, player.getPosition()))
            .flatMap(Set::stream).collect(Collectors.toSet());
    }

    public synchronized boolean loadBlock(SceneBlock block) {
        return Optional.ofNullable(block).filter(b -> !getLoadedBlocks().contains(b)).stream()
            .peek(b -> onLoadBlock(b, getPlayers()))
            .peek(getLoadedBlocks()::add)
            .findFirst().isPresent();
    }

    /**
     * Periodically checks for groups spawned, mainly
     * 1) checks if currently loaded groups are out of player's sight
     *  - if so, remove those groups
     * 2) check if there are new groups came into player's sight
     *  - if so, load up those groups
     * */
    public synchronized void checkGroups() {
        List<Player> playerMoved = getPlayers().stream()
            .filter(p -> p.getLastCheckedPosition() == null || !p.getLastCheckedPosition().equal2d(p.getPosition()))
            .peek(p -> p.setLastCheckedPosition(p.getPosition().clone()))
            .toList();
        if(playerMoved.isEmpty()) return;

        Set<Integer> visible = getPlayers().stream()
            .map(this::getPlayerActiveGroups)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        getLoadedGroups().stream().filter(group -> !visible.contains(group.id) && !group.dynamic_load)
            .forEach(group -> unloadGroup(getScriptManager().getBlocks().get(group.block_id), group.id));

        Optional.of(visible.stream()
                .filter(g -> getLoadedGroups().stream().noneMatch(gr -> gr.id == g))
                .filter(g -> !getReplacedGroup().contains(g))
                .map(g -> Optional.ofNullable(getScriptManager().getBlocks()).stream()
                    .map(Map::values).flatMap(Collection::stream)
                    .peek(this::loadBlock)
                    .map(b -> b.groups.get(g))
                    .filter(Objects::nonNull).filter(group -> !group.dynamic_load)
                    .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList()).stream()
            .filter(toLoad -> !toLoad.isEmpty())
            .peek(this::onLoadGroup)
            .forEach(e -> onRegisterGroups());
    }

    public Set<SceneGroup> onLoadBlock(SceneBlock block, List<Player> players) {
        if (!block.isLoaded()) {
            getScriptManager().loadBlockFromScript(block);
            Grasscutter.getLogger().info("Scene {} Block {} loaded.", getId(), block.id);
        }
        return getScriptManager().getLoadedGroupSetPerBlock().computeIfAbsent(block.id, f -> new HashSet<>());
    }

    /**
     * Load specific (dynamic loaded) group
     * */
    public int loadDynamicGroup(int groupId) {
        return getScriptManager().getGroupInstanceById(groupId) != null ? -1 :
            Optional.ofNullable(getScriptManager().getGroupById(groupId))
            .map(group -> group.init_config)
            .map(config -> config.suite).orElse(-1);
    }

    /**
     * Remove specific (dynamic loaded) group(A), process as follow
     * 1) unload group(A)
     * 2) check if group(A) replaced other groups(B)
     * 3) if so, reload groups(B)
     * */
    public boolean unregisterDynamicGroup(int groupId){
        return Optional.ofNullable(getScriptManager().getGroupById(groupId)).stream()
            .map(group -> getScriptManager().getBlocks().get(group.block_id))
            .peek(block -> unloadGroup(block, groupId))
            .map(block -> block.groups)
            .peek(groupMap -> onLoadGroup(Optional.ofNullable(groupMap.get(groupId))
                .map(group -> group.getReplaceableGroups(groupMap.values())).stream()
                .flatMap(List::stream)
                .filter(replacement -> getReplacedGroup().remove(replacement.id))
                .toList()))
            .peek(groupMap -> Grasscutter.getLogger().info("Unregistered group: {}", groupId))
            .peek(groupMap -> Grasscutter.getLogger().info("Replaced groups: {}", getReplacedGroup()))
            .findFirst().isPresent();
    }

    /**
     * Check if a spawning group(1) will replace other groups(2). If so, unload groups(2)
     * */
    public void onRegisterGroups() {
        // Create the graph
        Set<Integer> groupList = new HashSet<>();
        Set<KahnsSort.Node> nodes = GameData.getGroupReplacements().values().stream()
            .filter(replacement -> getLoadedGroups().stream().filter(group -> group.dynamic_load)
                .anyMatch(group -> group.id == replacement.id)) // dynamic groups
//            .filter(replacement -> getReplacedGroup().stream().noneMatch(replacement.replace_groups::contains))
            .peek(replacement -> Grasscutter.getLogger().info("Graph ordering replacement {}", replacement))
            .peek(replacement -> groupList.add(replacement.id))
            .peek(replacement -> groupList.addAll(replacement.replace_groups))
            .map(replacement -> replacement.replace_groups
                .stream().map(id -> new KahnsSort.Node(replacement.id, id)).toList())
            .flatMap(List::stream)
            .collect(Collectors.toSet());

        // Now we can start unloading and loading groups :D
        Optional.ofNullable(KahnsSort.doSort(new KahnsSort.Graph(
                nodes.stream().toList(), groupList.stream().toList()))).stream().flatMap(List::stream)
            .map(groupId -> getLoadedGroups().stream().filter(g -> g.id == groupId).findFirst()) // isGroupJoinReplacement
            .filter(Optional::isPresent).map(Optional::get)
            .map(targetGroup -> targetGroup.getReplaceableGroups(getLoadedGroups()))
            .flatMap(List::stream)
            .filter(replacement -> !getReplacedGroup().contains(replacement.id))
            .peek(replacement -> getReplacedGroup().add(replacement.id))
            .peek(replacement -> Grasscutter.getLogger().info("Graph ordering: unloaded {}", replacement.id))
            .peek(replacement -> Grasscutter.getLogger().info("Replaced group: {}", getReplacedGroup()))
            .forEach(replacement -> unloadGroup(getScriptManager().getBlocks().get(replacement.block_id), replacement.id));
    }

    /**
     * Load and register the group's triggers and regions
     * */
    public void loadTriggerFromGroup(SceneGroup group, String triggerName) {
        getScriptManager().registerTrigger(group.triggers.values().stream().filter(p -> p.getName().contains(triggerName)).toList());
        group.regions.values().stream()
            .filter(q -> q.config_id == Integer.parseInt(triggerName.substring(13)))
            .map(region -> new EntityRegion(this, region))
            .forEach(getScriptManager()::registerRegion);
    }

    /**
     * Load specific group(s), used when this
     * 1) group is came into player's sight (or visible)
     * 2) group is being registered
     * */
    public void onLoadGroup(Collection<SceneGroup> groups) {
        if (groups == null || groups.isEmpty()) return;

        groups.stream()
            .filter(Objects::nonNull)
            .filter(group -> !getLoadedGroups().contains(group))
            .peek(getScriptManager()::loadGroupFromScript)
            // We load the script files for the groups here
            .forEach(group -> onLoadBlock(getScriptManager().getBlocks().get(group.block_id), getPlayers()).add(group));

        // TODO Spawn gadgets AFTER triggers are added
        List<GameEntity> entities = new ArrayList<>();
        List<GameEntity> entitiesBorn = new ArrayList<>();

        groups.stream().filter(group -> !getLoadedGroups().contains(group))
            .filter(group -> group.init_config != null)
            .map(group -> Optional.ofNullable(getScriptManager().getCachedGroupInstanceById(group.id))
                .stream().peek(cachedInstance -> cachedInstance.setLuaGroup(group))
                .findFirst().orElse(getScriptManager().getGroupInstanceById(group.id)))
            .peek(gi -> getLoadedGroups().add(gi.getLuaGroup())) // Load suites
            .forEach(gi -> getScriptManager().refreshGroup(gi, 0, false, entitiesBorn)); //This is what the official server does

        getScriptManager().meetEntities(entities);
        getScriptManager().addEntities(entitiesBorn);
        groups.forEach(g -> getScriptManager().callEvent(new ScriptArgs(g.id, EventType.EVENT_GROUP_LOAD, g.id)));
        Grasscutter.getLogger().info("Scene {} loaded {} group(s)", getId(), groups.size());
    }

    /**
     * Remove specific group, used when this
     * 1) group is not within player's sight (or not visible)
     * 2) group is replaced by other groups
     * 3) group is being unregistered
     * */
    public void unloadGroup(SceneBlock block, int groupId) {
        removeEntities(getEntities().values().stream().filter(Objects::nonNull)
            .filter(e -> e.getBlockId() == block.id && e.getGroupId() == groupId).toList(), VisionType.VISION_TYPE_REMOVE);

        Optional.ofNullable(block.groups.get(groupId)).stream()
            .peek(group -> Optional.ofNullable(group.triggers).map(Map::values)
                .ifPresent(getScriptManager()::deregisterTriggers))
            .peek(group -> Optional.ofNullable(group.regions).map(Map::values)
                .ifPresent(getScriptManager()::deregisterRegions))
            .peek(getLoadedGroups()::remove)
            .peek(getScriptManager()::unregisterGroup)
            .peek(group -> Optional.ofNullable(getScriptManager().getLoadedGroupSetPerBlock().get(group.block_id))
                .stream().peek(loadedGroupSet -> loadedGroupSet.remove(group)).filter(Set::isEmpty)
                .peek(s -> Grasscutter.getLogger().info("Scene {} Block {} is unloaded.", getId(), group.block_id))
                .forEach(s -> getScriptManager().getLoadedGroupSetPerBlock().remove(group.block_id)))
            .forEach(group -> broadcastPacket(new PacketGroupUnloadNotify(List.of(group.id))));
    }

    // Gadgets
    /**
     * Create gadget belongs to the player, mostly spawned by player's skills and abilities
     * */
    public void onPlayerCreateGadget(EntityClientGadget gadget) {
        Optional.of(gadget).stream()
            .peek(this::addEntityDirectly) // Directly add
            .peek(gadget.getOwner().getTeamManager().getGadgets()::add) // Add to owner's gadget list
            .filter(g -> getPlayers().stream().anyMatch(p -> p != g.getOwner())) // if there is other players in scene
            .forEach(g -> broadcastPacketToOthers(g.getOwner(), new PacketSceneEntityAppearNotify(g)));
    }

    /**
     * Destroy gadget belongs to the player, mostly including the
     * gadget spawned by player's skills and abilities
     * */
    public void onPlayerDestroyGadget(int entityId) {
        Optional.ofNullable(getEntities().get(entityId)).stream()
            .filter(EntityClientGadget.class::isInstance).map(EntityClientGadget.class::cast)
            .peek(this::removeEntityDirectly) // Get and remove entity
            .peek(gadget -> gadget.getOwner().getTeamManager().getGadgets().remove(gadget)) // Remove from owner's gadget list
            .filter(gadget -> getPlayers().stream().anyMatch(p -> p != gadget.getOwner())) // if there is other players in scene
            .forEach(gadget -> broadcastPacketToOthers(gadget.getOwner(),
                new PacketSceneEntityDisappearNotify(gadget, VisionType.VISION_TYPE_DIE)));
    }

    // Broadcasting
    /**
     * Send packets to all players in world
     * */
    public void broadcastPacket(BasePacket packet) {
        // Send to all players - might have to check if player has been sent data packets
        broadcastPacketToOthers(null, packet);
    }

    /**
     * Send packets to all players in world excluding one, which normally is the owner
     * */
    public void broadcastPacketToOthers(Player excludedPlayer, BasePacket packet) {
        getPlayers().stream().filter(p -> p != excludedPlayer).forEach(p -> p.sendPacket(packet));
    }

    /**
     * Spawn (reward) items when killing monster or opening chest
     * */
    public void addItemEntity(int itemId, int amount, GameEntity bornForm) {
        Optional.ofNullable(GameData.getItemDataMap().get(itemId))
            .ifPresent(itemData -> {
                float range = itemData.isEquip() ? (1.5f + (.05f * amount)) : 0.0f;
                Position pos = bornForm.getPosition().nearby2d(range).addZ(.9f);
                IntStream.range(0, amount)
                    .mapToObj(i -> new EntityItem(this, null, itemData, pos, itemData.isEquip() ? 1 : amount))
                    .forEach(this::addEntity);
            });
    }

    public void loadNpcForPlayerEnter(Player player) {
        this.npcBornEntrySet.addAll(loadNpcForPlayer(player));
    }

    /**
     * Load Npc groups
     * */
    private List<SceneNpcBornEntry> loadNpcForPlayer(Player player) {
        return Optional.ofNullable(GameData.getSceneNpcBornData().get(getId())).stream()
            .map(data -> SceneIndexManager.queryNeighbors(data.getIndex(), player.getPosition().toDoubleArray(),
                Grasscutter.getConfig().server.game.loadEntitiesForPlayerRange))
            .peek(npcList ->
                Optional.of(npcList.stream().filter(i -> !this.npcBornEntrySet.contains(i)).toList()).stream()
                    .filter(npcBornEntries -> !npcBornEntries.isEmpty())
                    .peek(npcBornEntries -> Grasscutter.getLogger().debug("Loaded Npc Group Suite {}", npcBornEntries))
                    .findFirst().ifPresent(npcBornEntries -> broadcastPacket(new PacketGroupSuiteNotify(npcBornEntries)))
            ).flatMap(List::stream).toList();
    }

    /**
     * Load groups for specific quests
     * */
    public void loadGroupForQuest(List<QuestGroupSuite> sceneGroupSuite) {
        if (!getScriptManager().isInit()) return;

        sceneGroupSuite.stream().filter(i -> getScriptManager().getGroupById(i.getGroup()) != null)
            .forEach(i -> getScriptManager().refreshGroup(i.getGroup(), i.getSuite(), false));
    }

    public void unlockForce(int force) {
        this.unlockedForces.add(force);
        broadcastPacket(new PacketSceneForceUnlockNotify(force, true));
    }

    public void lockForce(int force) {
        this.unlockedForces.remove(force);
        broadcastPacket(new PacketSceneForceLockNotify(force));
    }

    /**
     * Execute when player interact with worktop like
     * console, challenge, or blossom
     * */
    public void selectWorktopOptionWith(SelectWorktopOptionReqOuterClass.SelectWorktopOptionReq req) {
        GameEntity entity = getEntityById(req.getGadgetEntityId());
        Optional.ofNullable(entity)
            .filter(EntityGadget.class::isInstance).map(EntityGadget.class::cast)
            .map(EntityGadget::getContent)
            .filter(GadgetWorktop.class::isInstance).map(GadgetWorktop.class::cast)
            .filter(worktop -> worktop.onSelectWorktopOption(req))
            .ifPresent(worktop -> entity.getScene().removeEntity(entity, VisionType.VISION_TYPE_REMOVE));
    }

    /**
     * Save groups to database
     * */
    public void saveGroups() {
        getScriptManager().getCachedGroupInstances().values().forEach(SceneGroupInstance::save);
    }
}
