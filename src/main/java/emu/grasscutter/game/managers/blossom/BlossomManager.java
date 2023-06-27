package emu.grasscutter.game.managers.blossom;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScriptSceneData;
import emu.grasscutter.data.binout.ScriptSceneData.ScriptObject;
import emu.grasscutter.data.binout.ScriptSceneData.ScriptObject.*;
import emu.grasscutter.data.excels.*;
import emu.grasscutter.data.excels.BlossomRefreshData.*;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.gadget.GadgetWorktop;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.managers.ResinManager;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import emu.grasscutter.game.player.BasePlayerDataManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.proto.BlossomBriefInfoOuterClass.BlossomBriefInfo;
import emu.grasscutter.net.proto.BlossomChestInfoOuterClass.BlossomChestInfo;
import emu.grasscutter.scripts.CommonScriptManager;
import emu.grasscutter.scripts.data.common.BlossomGroupV2;
import emu.grasscutter.server.packet.send.PacketBlossomBriefInfoNotify;
import emu.grasscutter.server.packet.send.PacketWorldOwnerBlossomScheduleInfoNotify;
import emu.grasscutter.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class BlossomManager extends BasePlayerDataManager {
    private Scene scene;
    /**
     * Holds information of all blossom camps
     * */
    private final List<BlossomSchedule> blossomSchedule = new ArrayList<>();
    /**
     * Holds information of all spawned blossom camps [Key: GadgetGroupId, Value: EntityGadget]
     * */
    private final Int2ObjectMap<EntityGadget> spawnedBlossoms = new Int2ObjectOpenHashMap<>();
    /**
     * Holds information of all active(started) blossom camps [Key: GadgetGroupId, Value: BlossomSchedule]
     * */
    private final Int2ObjectMap<BlossomSchedule> activeBlossomChallenge = new Int2ObjectOpenHashMap<>();
    /**
     * Holds information of all active(started) blossom camps [Key: GadgetId, Value: BlossomSchedule]
     * */
    private final Int2ObjectMap<BlossomSchedule> spawnedChest = new Int2ObjectOpenHashMap<>();
    /**
     * Script used to connect with ScriptLib
     * */
    private BlossomGroupV2 blossomScript = null;

    public BlossomManager(Player player) {
        setPlayer(player);
        Optional.ofNullable(CommonScriptManager.getCommonScripts("BlossomGroupV2"))
            .ifPresent(commonScript -> {
                if (!(commonScript instanceof BlossomGroupV2 blossomGroupV2)) return;
                this.blossomScript = blossomGroupV2;
            });
    }

    /**
     * BlossomBriefInfo proto
     * */
    public List<BlossomBriefInfo> getBriefInfo() {
        return getBlossomSchedule().stream()
            .map(BlossomSchedule::toBriefProto)
            .toList();
    }

    /**
     * Notify player's(not necessary world owner) all BlossomBriefInfo (all including not spawned)
     * */
    public void notifyPlayerIcon() {
        getPlayer().sendPacket(new PacketBlossomBriefInfoNotify(getBriefInfo()));
    }

    /**
     * Create all blossom camps and send packets
     * */
    public void onPlayerLogin() {
        buildBlossomSchedule();
        notifyPlayerIcon();
    }

    /**
     * Update Blossom camp's state (started or ended blossom challenge for example), triggered by ScriptLib
     * */
    public boolean setBlossomState(int groupId, int state) {
        val blossom = getBlossomSchedule().stream()
            .filter(schedule -> schedule.getGroupId() == groupId)
            .filter(schedule -> schedule.getState() != state) // get blossom not already started
            .filter(schedule -> getSpawnedBlossoms().containsKey(schedule.getGroupId()))
            .findFirst();

        if (blossom.isEmpty()) return false;

        blossom.get().setState(state);
        getActiveBlossomChallenge().putIfAbsent(groupId, blossom.get());
        getScene().broadcastPacket(new PacketWorldOwnerBlossomScheduleInfoNotify(blossom.get().toScheduleProto()));
        return true;
    }

    /**
     * Update Blossom camp's progress (on monster die for example), triggered by ScriptLib
     * */
    public boolean addBlossomProgress(int groupId) {
        BlossomSchedule blossomSchedule = getActiveBlossomChallenge().get(groupId);
        if (blossomSchedule == null) return false;

        blossomSchedule.addProgress();
        getScene().broadcastPacket(new PacketWorldOwnerBlossomScheduleInfoNotify(blossomSchedule.toScheduleProto()));

        if (blossomSchedule.getProgress() >= blossomSchedule.getFinishProgress()) {
            EntityGadget gadget = getSpawnedBlossoms().get(groupId);
            if (getBlossomScript() == null) return false;

            getBlossomScript().createReward(gadget, groupId);
        }
        return true;
    }

    /**
     * Get Blossom Chest gadget info (after challenge finish)
     * */
    public BlossomChestInfo getChestInfo(int chestGadgetId) {
        BlossomSchedule schedule = getSpawnedChest().get(chestGadgetId);
        if (schedule == null) return BlossomChestInfo.newBuilder().build();

        List<Integer> playersUid = getScene().getPlayers().stream().map(Player::getUid).toList();
        schedule.getRemainingUid().addAll(playersUid);
        return BlossomChestInfo.newBuilder()
            .setResin(schedule.getResin())
            .addAllQualifyUidList(playersUid)
            .addAllRemainUidList(playersUid)
            .setRefreshId(schedule.getRefreshId())
            .setBlossomRefreshType(schedule.getRefreshType().getValue())
            .build();
    }

    /**
     * Get Blossom Camp gadget (not chest gadget)
     * */
    public EntityGadget getBlossomGadget(Scene scene, BlossomSchedule schedule) {
        EntityGadget blossom = new EntityGadget(scene, schedule.getGadgetId(),
            schedule.getPosition(), schedule.getRotation());
        blossom.setGroupId(schedule.getGroupId());
        blossom.setConfigId(schedule.getConfigId());
        blossom.setState(204); // TODO find another way to do for it to be less hard coded i guess
        blossom.buildContent();
        if (blossom.getContent() instanceof GadgetWorktop gadgetWorktop) {
            gadgetWorktop.addWorktopOptions(new int[]{187});
            gadgetWorktop.setOnSelectWorktopOptionEvent((GadgetWorktop context, int option) -> {
                Grasscutter.getLogger().info("Starting blossom schedule: {}", schedule.getRefreshId());
                Grasscutter.getLogger().info("Option: {}, CampId: {}, ConfigId: {}",
                    option, schedule.getCircleCampId(), schedule.getConfigId());
                if (getBlossomScript() == null) return false;

                getBlossomScript().onSelectOption(context.getGadget());
                return true;
            });
        }
        return blossom;
    }

    /**
     * Get Blossom Camp gadget (not chest gadget)
     * */
    public EntityGadget getBlossomGadget(BlossomSchedule schedule) {
        return getBlossomGadget(getScene(), schedule);
    }

    /**
     * Give player reward after challenge finish
     * */
    public boolean onReward(Player player, EntityGadget gadget, boolean useCondensedResin) {
        BlossomSchedule schedule = getSpawnedChest().get(gadget.getGadgetId());
        if (schedule == null) return false;

        ResinManager resinManager = player.getResinManager();
        boolean payable = useCondensedResin ?
            resinManager.useCondensedResin(1) :
            resinManager.useResin(schedule.getResin());

        if (!payable) return false;

        RewardPreviewData blossomRewards = GameData.getRewardPreviewDataMap().get(schedule.getRewardId());
        if (blossomRewards == null) return false;

        player.getInventory().addItems(Stream.of(blossomRewards.getPreviewItems())
            .map(blossomReward -> new GameItem(
                blossomReward.getItemId(),
                blossomReward.getCount() * (useCondensedResin ? 2 : 1)))
            .collect(Collectors.toList()), ActionReason.OpenBlossomChest);

        schedule.getRemainingUid().remove(player.getUid());
        if (schedule.getRemainingUid().isEmpty()) {
            getBlossomScript().onBlossomChestDie(gadget);
            getSpawnedChest().remove(gadget.getGadgetId());
            getActiveBlossomChallenge().remove(gadget.getGroupId());
            getSpawnedBlossoms().remove(gadget.getGroupId());
            getBlossomSchedule().remove(schedule);

            List<BlossomSchedule> newScheduleBlossom = new ArrayList<>();
            GameData.getBlossomGroupsDataMap().get(schedule.getCircleCampId())
                .getNextCampIdVec().stream()
                .map(campId -> GameData.getBlossomGroupsDataMap().get(campId.intValue()))
                .filter(Objects::nonNull)
                .forEach(group -> {
                    int blockGroupId = group.getNewGroupVec().stream().findAny().orElse(0);
                    List<SceneGadgetData> gadgetDataList = getBlossomGadgetData(3, blockGroupId);
                    if (gadgetDataList == null) return;

                    SceneGadgetData gadgetData = gadgetDataList.stream()
                        .filter(x -> x.getGadgetId() == schedule.getGadgetId())
                        .findFirst().orElse(null);

                    BlossomSchedule newSchedule = new BlossomSchedule(schedule, group, gadgetData, 3, blockGroupId);
                    getBlossomSchedule().add(newSchedule);
                    newScheduleBlossom.add(newSchedule);
                    getSpawnedBlossoms().put(newSchedule.getGroupId(), getBlossomGadget(newSchedule));
                });
            getBlossomSchedule().sort(Comparator.comparing(BlossomSchedule::getRefreshId)
                .thenComparing(BlossomSchedule::getCircleCampId));
            notifyPlayerIcon();
            notifyOwnerIcon(newScheduleBlossom);
        }
        return true;
    }

    /**
     * Rebuild all Blossom Camp gadget (not chest gadget) for scene
     * */
    public void buildBlossomEntity(Player player) {
        if (getScene() == null || player != getPlayer().getWorld().getOwner()) return;
        getSpawnedBlossoms().clear();
        getBlossomSchedule().stream()
            .filter(schedule -> schedule.getSceneId() == getScene().getId() && schedule.getGadgetId() > 0)
            .forEach(schedule -> {
                EntityGadget blossom = getBlossomGadget(getScene(), schedule);
                getSpawnedBlossoms().put(schedule.getGroupId(), blossom);
                getScene().getEntities().put(blossom.getId(), blossom);
            });

//        getSpawnedBlossoms().values().forEach(scene::addEntity);
    }

    /**
     * Notify world owner's all BlossomBriefInfo (all including not spawned)
     * */
    public void notifyOwnerIcon() {
        notifyOwnerIcon(getBlossomSchedule());
    }

    /**
     * Notify world owner's all BlossomBriefInfo (all including not spawned)
     * */
    public void notifyOwnerIcon(Collection<BlossomSchedule> notifyList) {
        notifyList.stream()
            .filter(schedule -> getSpawnedBlossoms().keySet().contains(schedule.getGroupId()))
            .map(BlossomSchedule::toScheduleProto)
            .forEach(proto -> getScene().broadcastPacket(new PacketWorldOwnerBlossomScheduleInfoNotify(proto)));
    }

    public int getWorldLevel() {
        return getPlayer().getWorldLevel(); // maybe should get the owner's?
    }

    public int getPlayerLevel() {
        return getPlayer().getLevel(); // maybe should get the owner's?
    }

    /**
     * Check if player is qualified to open certain blossom camp
     * */
    public boolean refreshCondMet(@NotNull RefreshCond cond) {
        return switch (cond.getRefreshCondType()) {
            case BLOSSOM_REFRESH_COND_NONE -> true;
            case BLOSSOM_REFRESH_COND_PLAYER_LEVEL_EQUAL_GREATER ->
                cond.getParam().stream().allMatch(p -> getPlayerLevel() >= p);
            case BLOSSOM_REFRESH_COND_PLAYER_LEVEL_LESS_THAN ->
                cond.getParam().stream().allMatch(p -> getPlayerLevel() < p);
            case BLOSSOM_REFRESH_COND_OPEN_STATE ->
                cond.getParam().stream().allMatch(p -> getPlayer().getProgressManager().getOpenState(p) == 1);
            // case BLOSSOM_REFRESH_COND_ACTIVITY_COND, BLOSSOM_REFRESH_COND_SCENE_TAG_ADDED -> TODO
            default -> false;
        };
    }

    /**
     * Get all gadget information of a blossom camp using group id
     * */
    public List<SceneGadgetData> getBlossomGadgetData(int sceneId, int groupId) {
        ScriptSceneData fullGlobals = GameData.getScriptSceneDataMap().get("flat.luas.scenes.full_globals.lua.json");
        if (fullGlobals == null) return null;

        ScriptObject scriptObject = fullGlobals.getScriptObjectList()
            .get(sceneId + "/scene" + sceneId + "_group" + groupId +".lua");
        if (scriptObject == null) return null;

        val gadgets = scriptObject.getGadgets();

        return gadgets == null ? null : ((List<?>) gadgets).stream()
            .filter(b -> b instanceof SceneGadgetData)
            .map(b -> ((SceneGadgetData) b))
            .toList();
    }

    /**
     * Get resin cost of current blossom camp
     * */
    public int getResinCost(BlossomRefreshType refreshType) {
        return GameData.getBlossomChestDataMap().values().stream()
            .filter(c -> c.getBlossomRefreshType() == refreshType)
            .map(BlossomChestData::getResin)
            .findFirst().orElse(0);
    }

    /**
     * Build all blossom camp information
     * */
    public void buildBlossomSchedule() {
        WorldLevelData worldData = GameData.getWorldLevelDataMap().get(getWorldLevel());
        int monsterLevel = worldData == null ? 26 : worldData.getMonsterLevel();
        List<Integer> appendedGroupId = new ArrayList<>();
        List<Integer> appendedSectionId = new ArrayList<>();

        // list out all the available sections for groups
        List<Integer> groupAvailableSection = GameData.getBlossomGroupsDataMap().values().stream()
            .map(BlossomGroupsData::getSectionId)
            .toList();

        getBlossomSchedule().addAll(GameData.getBlossomRefreshDataMap().values().stream()
            .filter(data -> data.getRefreshCondVec().stream().allMatch(this::refreshCondMet))
            .filter(data -> getPlayerLevel() >= GameData.getBlossomOpenDataMap().get(data.getCityId()).getOpenLevel())
            .map(data -> {
                BlossomRefreshType refreshType = data.getBlossomRefreshType();
                if (refreshType == BlossomRefreshType.BLOSSOM_REFRESH_NONE) return null; // unimplemented types

                // groups that met information
                List<BlossomGroupsData> groupList = new ArrayList<>();

                // get section order for the current city
                List<Integer> sectionList = GameData.getBlossomSectionOrderDataMap().values().stream()
                    .filter(orderData -> orderData.getCityId() == data.getCityId())
                    .map(BlossomSectionOrderData::getSectionId)
                    .filter(groupAvailableSection::contains) // some section does not have groups
                    .filter(sectionId -> {
                        List<BlossomGroupsData> tempGroupList = GameData.getBlossomGroupsDataMap().values().stream()
                            .filter(group -> group.getCityId() == data.getCityId()
                                && group.getSectionId() == sectionId
                                && group.getRefreshTypeVec().contains(refreshType.getValue())
                                && !appendedGroupId.contains(group.getId()))
                            .filter(BlossomGroupsData::isInitialRefresh)
                            .toList();
                        if (tempGroupList.size() < data.getRefreshCount()) return false;

                        groupList.addAll(tempGroupList);
                        return true;
                    })
                    .filter(sectionId -> !appendedSectionId.contains(sectionId)) // don't want to get the same section
                    .toList();

                if (sectionList.isEmpty() || groupList.isEmpty()) {
                    Grasscutter.getLogger().info("RefreshType: {}", refreshType);
                    return null;
                }

                // draw a new random section id
                int randomSectionId = Utils.drawRandomListElement(sectionList);
                appendedSectionId.add(randomSectionId);

                return IntStream.range(0, data.getRefreshCount())
                    .mapToObj(e -> {
                        // draw a new random group
                        BlossomGroupsData randomGroup = Utils.drawRandomListElement(groupList);
                        appendedGroupId.add(randomGroup.getId());
                        groupList.remove(randomGroup);

                        // build blossom schedule
                        int blockGroupId = randomGroup.getNewGroupVec().stream().findAny().orElse(0);
                        List<SceneGadgetData> gadgetDataList = getBlossomGadgetData(3, blockGroupId);
                        if (gadgetDataList == null) return null;

                        SceneGadgetData gadgetData = gadgetDataList.stream()
                            .filter(x -> x.getGadgetId() == refreshType.getGadgetId())
                            .findFirst().orElse(null);

                        return new BlossomSchedule(data, randomGroup, gadgetData, 3,
                            getResinCost(refreshType), monsterLevel - data.getReviseLevel(),
                            getWorldLevel(), blockGroupId, refreshType);
                    })
                    .filter(Objects::nonNull)
                    .toList();
            })
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .sorted(Comparator.comparing(BlossomSchedule::getRefreshId).thenComparing(BlossomSchedule::getCircleCampId))
            .toList());
    }
}
