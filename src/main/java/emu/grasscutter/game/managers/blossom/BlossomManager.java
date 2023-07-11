package emu.grasscutter.game.managers.blossom;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.*;
import emu.grasscutter.data.excels.BlossomRefreshData.*;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.managers.ResinManager;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import emu.grasscutter.game.player.BasePlayerDataManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.proto.BlossomBriefInfoOuterClass.BlossomBriefInfo;
import emu.grasscutter.net.proto.BlossomChestInfoOuterClass.BlossomChestInfo;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneGadget;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.ScriptArgs;
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
     * Holds information of all blossom camps, [Key: GroupId, Value: BlossomSchedule]
     * */
    private final Int2ObjectMap<BlossomSchedule> blossomSchedule = new Int2ObjectOpenHashMap<>();
    /**
     * Holds information of all spawned chest after finishing blossom challenge,
     * necessary for building chest brief info,
     * [Key: ConfigId, Value: BlossomSchedule]
     * */
    private final Int2ObjectMap<BlossomSchedule> spawnedChest = new Int2ObjectOpenHashMap<>();

    public BlossomManager(Player player) {
        setPlayer(player);
    }

    /**
     * BlossomBriefInfo proto
     * */
    public List<BlossomBriefInfo> getBriefInfo() {
        return getBlossomSchedule().values().stream()
            .map(BlossomSchedule::toBriefProto)
            .sorted(Comparator.comparing(BlossomBriefInfo::getRefreshId)
                .thenComparing(BlossomBriefInfo::getCircleCampId))
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
        val blossom = getBlossomSchedule().get(groupId);

        if (blossom == null) return false;

        blossom.setState(state);
        getScene().broadcastPacket(new PacketWorldOwnerBlossomScheduleInfoNotify(blossom.toScheduleProto()));
        return true;
    }

    /**
     * Update Blossom camp's progress (on monster die for example), triggered by ScriptLib
     * */
    public boolean addBlossomProgress(int groupId) {
        BlossomSchedule blossomSchedule = getBlossomSchedule().get(groupId);
        if (blossomSchedule == null) return false;

        blossomSchedule.addProgress();
        getScene().broadcastPacket(new PacketWorldOwnerBlossomScheduleInfoNotify(blossomSchedule.toScheduleProto()));

        if (blossomSchedule.getProgress() >= blossomSchedule.getFinishProgress()) {
            SceneGroup group = getScene().getScriptManager().getGroupById(groupId);
            getScene().getScriptManager().callEvent(new ScriptArgs(groupId, EventType.EVENT_BLOSSOM_PROGRESS_FINISH,
                group.gadgets.values().stream()
                    .filter(g -> g.chest_drop_id > 0)
                    .map(g -> g.config_id).findFirst().orElse(0)));
        }
        return true;
    }

    /**
     * Get Blossom Chest gadget proto info (after challenge finish)
     * */
    public BlossomChestInfo getChestInfo(int chestConfigId) {
        BlossomSchedule schedule = getSpawnedChest().get(chestConfigId);
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
     * Give player reward after challenge finish
     * */
    public boolean onReward(Player player, @NotNull EntityGadget gadget, boolean useCondensedResin) {
        BlossomSchedule schedule = getSpawnedChest().get(gadget.getConfigId());
        // if player is not qualified for the reward
        if (schedule == null || !schedule.getRemainingUid().contains(player.getUid())) return false;

        // give rewards
        ResinManager resinManager = player.getResinManager();
        boolean payable = useCondensedResin ? resinManager.useCondensedResin(1) : resinManager.useResin(schedule.getResin());
        if (!payable) return false;

        RewardPreviewData blossomRewards = GameData.getRewardPreviewDataMap().get(schedule.getRewardId());
        if (blossomRewards == null) return false;

        player.getInventory().addItems(Stream.of(blossomRewards.getPreviewItems())
            .map(blossomReward -> new GameItem(
                blossomReward.getItemId(),
                blossomReward.getCount() * (useCondensedResin ? 2 : 1)))
            .collect(Collectors.toList()), ActionReason.OpenBlossomChest);

        // build next camps
        schedule.getRemainingUid().remove(player.getUid());
        if (schedule.getRemainingUid().isEmpty()) {
            getSpawnedChest().remove(gadget.getConfigId());
            getBlossomSchedule().remove(schedule.getGroupId());
            getScene().getScriptManager().callEvent(new ScriptArgs(schedule.getGroupId(), EventType.EVENT_BLOSSOM_CHEST_DIE));
            getScene().unregisterDynamicGroup(schedule.getGroupId());

            GameData.getBlossomGroupsDataMap().get(schedule.getCircleCampId()).getNextCampIdVec().stream()
                .map(campId -> GameData.getBlossomGroupsDataMap().get(campId.intValue()))
                .map(groupsData -> {
                    // if next camp overlaps with existing schedule, get further next camp
                    return getBlossomSchedule().values().stream().anyMatch(s ->
                        groupsData.getNextCampIdVec().stream().anyMatch(camp -> s.getCircleCampId() == camp)) ?
                        GameData.getBlossomGroupsDataMap().get(
                            groupsData.getNewGroupVec().stream().findAny().orElse(0).intValue()) : groupsData;
                })
                .filter(Objects::nonNull).forEach(groupData -> {
                    int blockGroupId = groupData.getNewGroupVec().stream().findAny().orElse(0);

                    SceneGroup newGroup = SceneGroup.of(blockGroupId).load(3);
                    if (newGroup == null) return;

                    val gadgetsMap = newGroup.gadgets;
                    if (gadgetsMap == null) return;

                    SceneGadget newGadget = gadgetsMap.values().stream()
                        .filter(g -> g.gadget_id == schedule.getRefreshType().getGadgetId()).findFirst()
                        .orElse(null);

                    getBlossomSchedule().put(blockGroupId, new BlossomSchedule(
                        schedule, groupData, newGadget, 3, blockGroupId));
                    getScene().loadDynamicGroup(blockGroupId);
                });
            notifyPlayerIcon(); // notify all camps again
        }
        return true;
    }

    /**
     * Rebuild all Blossom Camp gadget (not chest gadget) for scene
     * */
    public void loadBlossomGroup() {
        if (getScene() == null) return;

        getBlossomSchedule().values().forEach(schedule -> {
//            Grasscutter.getLogger().info("{}", schedule.getGroupId());
            getScene().loadDynamicGroup(schedule.getGroupId());
        });
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
        List<Integer> appendedGroupId = new ArrayList<>(); // counter to avoid repeated groups and section
        List<Integer> appendedSectionId = new ArrayList<>();

        // list out all the available sections for groups
        List<Integer> groupAvailableSection = GameData.getBlossomGroupsDataMap().values().stream()
            .map(BlossomGroupsData::getSectionId)
            .toList();

        getBlossomSchedule().putAll((Map<? extends Integer, ? extends BlossomSchedule>)
            GameData.getBlossomRefreshDataMap().values().stream()
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
                            .filter(BlossomGroupsData::isInitialRefresh).toList();
                        if (tempGroupList.size() < data.getRefreshCount()) return false;

                        groupList.addAll(tempGroupList);
                        return true;
                    }).filter(sectionId -> !appendedSectionId.contains(sectionId)) // don't want to get the same section
                    .toList();

                if (sectionList.isEmpty() || groupList.isEmpty()) {
                    Grasscutter.getLogger().info("RefreshType: {}", refreshType);
                    return null;
                }

                // draw a new random section id
                int randomSectionId = Utils.drawRandomListElement(sectionList);
                appendedSectionId.add(randomSectionId);

                return IntStream.range(0, data.getRefreshCount()).mapToObj(e -> {
                    // draw a new random group
                    BlossomGroupsData randomGroup = Utils.drawRandomListElement(groupList);
                    appendedGroupId.add(randomGroup.getId());
                    groupList.remove(randomGroup);

                    // new camp group id
                    int blockGroupId = randomGroup.getNewGroupVec().stream().findAny().orElse(0);

                    SceneGroup group = SceneGroup.of(blockGroupId).load(3);
                    if (group == null) return null;

                    val gadgetsMap = group.gadgets;
                    if (gadgetsMap == null) return null;

                    SceneGadget gadget = gadgetsMap.values().stream()
                        .filter(g -> g.gadget_id == refreshType.getGadgetId()).findFirst()
                        .orElse(null);

                    // build blossom schedule
                    return new BlossomSchedule(data, randomGroup, gadget, 3,
                        getResinCost(refreshType), monsterLevel - data.getReviseLevel(),
                        getWorldLevel(), blockGroupId, refreshType);
                }).filter(Objects::nonNull).toList();
            })
            .filter(Objects::nonNull).flatMap(List::stream)
            .collect(Collectors.toMap(BlossomSchedule::getGroupId, schedule -> schedule,
                (existingValue, newValue) -> newValue,   // Merge function: In case of duplicate keys, choose the new value
                Int2ObjectOpenHashMap::new)));
    }
}
