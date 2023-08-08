package emu.grasscutter.game.tower;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.excels.RewardData;
import emu.grasscutter.data.excels.TowerBuffData;
import emu.grasscutter.data.excels.TowerFloorData;
import emu.grasscutter.data.excels.TowerLevelData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.dungeons.settle_listeners.DungeonSettleListener;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.trigger.GuardTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.TimeTrigger;
import emu.grasscutter.game.dungeons.settle_listeners.TowerDungeonSettleListener;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.BasePlayerManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.net.proto.TowerCurLevelRecordOuterClass.TowerCurLevelRecord;
import emu.grasscutter.net.proto.TowerTeamOuterClass.TowerTeam;
import emu.grasscutter.server.packet.send.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO record tower combat record, the highest burst count, highest atk etc
 * */
public class TowerManager extends BasePlayerManager {
    private static final DungeonSettleListener TOWER_DUNGEON_SETTLE_LISTENER = new TowerDungeonSettleListener();
    private static final int STAR_COUNT_TO_UNLOCK_SCHEDULE_FLOOR = 6;
    /**
     * Holds energy information of tower avatar before entering tower, should be restored
     * after player leaves tower
     * entry: [avatarId, current energy]
     * */
    private final Map<Integer, Float> previousTeamEnergyMap = new Int2ObjectOpenHashMap<>();
    /**
     * Holds temporary energy information, will only write to db if player clears the current level
     * entry: [teamId from 1 to 2, recorded information]
     * */
    private final Map<Integer, TowerData.TowerTeamEnergyInfo> tempEnergyMap = new Int2ObjectOpenHashMap<>();

    public TowerManager(Player player) {
        super(player);
    }

    public TowerData getTowerData() {
        return this.player.getTowerData();
    }

    public TowerCurLevelRecord getCurRecordProto() {
        return getTowerData().toProto(this.player);
    }

    private TowerData.TowerLevelRecordInfo getCurLevelRecordInfo() {
        return getCurFloorRecordInfo().getPassedLevelRecordMap().get(getCurrentLevelIndex() - 1);
    }

    private TowerData.TowerFloorRecordInfo getCurFloorRecordInfo() {
        return Optional.ofNullable(GameData.getTowerFloorDataMap().get(getCurrentFloorId()))
            .map(TowerFloorData::getFloorIndex)
            .map(getTowerData().getRecordMap()::get)
            .orElse(null);
    }

    private int getCurrentFloorId() {
        return getTowerData().getCurrentFloorId();
    }

    private int getCurrentLevelId() {
        return getTowerData().getCurrentLevelId();
    }

    private int getCurrentLevelIndex() {
        return getTowerData().getCurrentLevelIndex();
    }

    private int getCurrentTeamIndex() {
        return getTowerData().getTeamIndex();
    }

    /**
     * Recorded energy of current tower team
     * */
    private TowerData.TowerTeamEnergyInfo getCurTeamEnergyMap() {
        // team index starts from 0
        return getTowerData().getTeamEnergyMap().get(getCurrentTeamIndex() + 1);
    }

    /**
     * Temporarily recorded energy of current tower team
     * */
    private TowerData.TowerTeamEnergyInfo getCurTempEnergyMap() {
        return this.tempEnergyMap.computeIfAbsent(getCurrentTeamIndex() + 1, f -> TowerData.TowerTeamEnergyInfo.create());
    }

    public Map<Integer, TowerData.TowerFloorRecordInfo> getRecordMap() {
        return getTowerData().getRecordMap();
    }

    private TowerSystem getTowerSystem() {
        return this.player.getServer().getTowerSystem();
    }

    public TowerData.TowerMonthlyBriefInfo getMonthlyBriefInfo() {
        return getTowerData().getMonthBriefInfo();
    }

    public void onLogin() {
        // unlock first floor by default, TODO maybe should also check against player level for more completed gameplay
        if (getRecordMap().isEmpty()) {
            getRecordMap().put(1, TowerData.TowerFloorRecordInfo.create(1001));
        }

        // check if there is new schedule, refresh floor 9 to 12
        if (getTowerSystem().getScheduleId() != getTowerData().getScheduleId()) {
            // can't really check against schedule end time, since it would probably be the new schedule end time at this point
            getTowerData().startNewSchedule(getTowerSystem().getScheduleId());
        }
    }

    /**
     * Invokes whenever player chooses a team and enter spiral abyss
     * */
    public boolean teamSelect(int floorId, List<TowerTeam> towerTeamInfo) {
        val floorData = GameData.getTowerFloorDataMap().get(floorId);
        if (floorData == null) return false;

        // should initialise the records for current selection
        getTowerData().refreshCurLevelRecord(towerTeamInfo, this.player, floorData);
        this.player.getTeamManager().setupTemporaryTeam(towerTeamInfo.stream().map(TowerTeam::getAvatarGuidListList).toList());
        notifyCurRecordChange();
        return true;
    }

    public void notifyCurRecordChange() {
        this.player.getSession().send(new PacketTowerCurLevelRecordChangeNotify(getCurRecordProto()));
    }

    /**
     * Use tower teams that player has selected
     * */
    private void rebuildAndUseTeam(int teamId) {
        // if player enter from big world, record the energy for avatar that
        // will be appearing in tower and restore it when player leaves
        if (this.player.getTeamManager().getTemporaryTeamIndex() < 0) {
            // team on hold should not be empty at this point
            getTowerData().getTeamOnHold().stream().map(TowerData.TowerTeamInfo::getAvatarIds).flatMap(List::stream)
                .map(this.player.getAvatars()::getAvatarById).forEach(avatar ->
                    this.previousTeamEnergyMap.put(avatar.getAvatarId(), avatar.getCurrentEnergy()));
        }

        // it will be empty if the player quit and resume the chamber
        if (this.player.getTeamManager().getTemporaryTeam().isEmpty()) {
            // recorded avatars should not be empty at this point
            this.player.getTeamManager().setupTemporaryTeam(getTowerData().getTeamOnHold().stream().parallel()
                .map(TowerData.TowerTeamInfo::getAvatarIds)
                .map(idList -> idList.stream().parallel().map(this.player.getAvatars()::getAvatarById)
                    .map(Avatar::getGuid).toList())
                .toList());
        }

        // make sure the avatars has full energy when it is the first level
        if (getCurrentLevelIndex() == 1) {
            getTowerData().getTeamOnHold().get(getCurrentTeamIndex()).getAvatarIds().stream().parallel()
                .map(this.player.getAvatars()::getAvatarById).forEach(avatar -> {
                    val skillDepot = avatar.getSkillDepot();
                    avatar.setCurrentEnergy(skillDepot.getElementType().getCurEnergyProp(), skillDepot.getEnergySkillData().getCostElemVal());
                });
        } else {
            // from level 2 onwards, TODO might be worth it to check if changes is needed
            Optional.ofNullable(getCurTeamEnergyMap()).map(TowerData.TowerTeamEnergyInfo::getAvatarEnergyMap).stream()
                .parallel().map(Map::entrySet).flatMap(Set::stream).forEach(e -> {
                    val avatar =  this.player.getAvatars().getAvatarById(e.getKey());
                    avatar.setCurrentEnergy(avatar.getSkillDepot().getElementType().getCurEnergyProp(), e.getValue());
                });
        }
        this.player.getTeamManager().useTemporaryTeam(teamId);
    }

    /**
     * Restore avatar's energy after quiting tower
     * */
    public void restoreEnergy() {
        this.previousTeamEnergyMap.entrySet().stream().parallel().forEach(e ->
            Optional.ofNullable(this.player.getAvatars().getAvatarById(e.getKey())).ifPresent(avatar ->
                avatar.setCurrentEnergy(avatar.getSkillDepot().getElementType().getCurEnergyProp(), e.getValue())));

        this.previousTeamEnergyMap.clear();
    }

    private void notifyAddBuffs(int towerBuffId) {
        Optional.ofNullable(GameData.getTowerBuffDataMap().get(towerBuffId))
            .ifPresent(towerBuffData -> this.player.getBuffManager().addBuff(towerBuffData.getBuffId()));
    }

    /**
     * Trigger when entering a chamber/level
     * */
    public void enterLevel(int enterPointId) {
        val levelData = GameData.getTowerLevelDataMap().get(getCurrentLevelId());
        if (levelData == null) return;

        this.player.getServer().getDungeonSystem().enterDungeon(
            this.player, enterPointId, levelData.getDungeonId(), TOWER_DUNGEON_SETTLE_LISTENER);
        rebuildAndUseTeam(getCurrentTeamIndex()); // use team user choose or recorded
        getTowerData().getTowerBuffs().values().forEach(this::notifyAddBuffs); // notify any recorded buffs

        this.player.getSession().send(new PacketTowerEnterLevelRsp(getTowerData()));
        // stop using skill
        this.player.getSession().send(new PacketCanUseSkillNotify(false));
        // notify the cond of stars
        this.player.getSession().send(new PacketTowerLevelStarCondNotify(getTowerData()));
    }

    public void addBuffs(int towerBuffId) {
        getTowerData().getTowerBuffs().put(getCurrentLevelIndex(), towerBuffId);
        notifyAddBuffs(towerBuffId);
    }

    /**
     * Trigger when player exit without completing current level
     * */
    public void removeCurrentLevelBuff() {
        getTowerData().getTowerBuffs().remove(getCurrentLevelIndex());
    }

    /**
     * Check if it should remove buffs when moving to new level or new floor
     * */
    private void tryRemoveBuffs() {
        val towerBuffList = getTowerData().getTowerBuffs().values().stream().map(towerBuffId ->
                GameData.getTowerBuffDataMap().get(towerBuffId.intValue()))
            .filter(Objects::nonNull).filter(buffData ->
                buffData.getLastingType() == TowerBuffLastingType.TOWER_BUFF_LASTING_LEVEL
                || buffData.getLastingType() == TowerBuffLastingType.TOWER_BUFF_LASTING_IMMEDIATE)
            .map(TowerBuffData::getId).toList();

        towerBuffList.forEach(getTowerData().getTowerBuffs()::remove);
        towerBuffList.stream().map(towerBuffId -> GameData.getTowerBuffDataMap().get(towerBuffId.intValue()))
            .filter(Objects::nonNull).forEach(towerBuffData ->
                this.player.getBuffManager().removeBuff(towerBuffData.getBuffId()));
    }

    private void notifyFloorChange(TowerData.TowerFloorRecordInfo floorRecordInfo) {
        this.player.sendPacket(new PacketTowerFloorRecordChangeNotify(floorRecordInfo, canEnterScheduleFloor()));
    }

    /**
     * Give tower first pass reward, namely chamber rewards
     * */
    public List<GameItem> giveFirstPassReward() {
        if (getCurLevelRecordInfo().isReceivedFirstPassReward()) return List.of();

        val rewardItems = Optional.ofNullable(GameData.getTowerFloorDataMap().get(getCurrentFloorId()))
            .map(TowerFloorData::getFloorIndex).map(floorIndex -> GameData.getTowerRewardData(getCurrentLevelIndex(), floorIndex))
            .map(rewardData -> rewardData.getFirstPassRewardByStarCount(getCurFloorRecordInfo().getStarCount()))
            .stream().flatMap(List::stream).distinct().map(rewardId -> GameData.getRewardDataMap().get(rewardId.intValue()))
            .filter(Objects::nonNull).map(RewardData::getRewardItemList).flatMap(List::stream)
            .collect(Collectors.toMap(ItemParamData::getItemId, ItemParamData::getItemCount, Integer::sum))
            .entrySet().stream().map(e -> new GameItem(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(GameItem::getItemId)).toList();

        if (!rewardItems.isEmpty()) {
            getCurLevelRecordInfo().setReceivedFirstPassReward(true);
            this.player.getInventory().addItems(rewardItems, ActionReason.TowerFirstPassReward);
        }

        return rewardItems;
    }

    private void updateNextLevel() {
        if (getTowerData().isUpperPart()) return; // don't update if this is only upper half of the challenge

        if (hasNextLevel()) {
            getTowerData().syncToNextLevel(getNextLevelId());
            getTowerData().updateEnergyMap(this.tempEnergyMap);
            this.tempEnergyMap.clear();
        } else { // get next floor if reached end level of this floor
            Optional.ofNullable(GameData.getTowerFloorDataMap().get(getNextFloorId()))
                .ifPresent(floorData -> getRecordMap().putIfAbsent(
                    floorData.getFloorIndex(), TowerData.TowerFloorRecordInfo.create(floorData.getId())));
            getTowerData().reset(); // player should be choosing new team at this point, should remove old team
        }
    }

    /**
     * Calculate the result after clearing level
     * */
    public int calculateStar(WorldChallenge challenge) {
        val levelData = GameData.getTowerLevelDataMap().get(getCurrentLevelId());
        if (levelData == null || challenge == null) return 0;

        int stars = (int) levelData.getConds().stream()
            .map(c -> getTowerData().isUpperPart() ? c.getUpperHalfCond() : c.getLowerHalfCond())
            .filter(c -> switch (c.getTowerCondType()) {
                case TOWER_COND_CHALLENGE_LEFT_TIME_MORE_THAN -> challenge.getChallengeTriggers().stream()
                    .filter(TimeTrigger.class::isInstance).map(TimeTrigger.class::cast)
                    .anyMatch(tt -> (challenge.getStartedAt() + tt.getGoal().get() - challenge.getFinishedTime()) >= c.getTargetLeftTime());
                case TOWER_COND_LEFT_HP_GREATER_THAN -> challenge.getGroupId() == c.getGroupId()
                    && challenge.getChallengeTriggers().stream().filter(GuardTrigger.class::isInstance)
                    .map(GuardTrigger.class::cast).filter(gt -> gt.getGoal().get() == c.getConfigId())
                    .anyMatch(gt -> gt.getLastSendPercent() >= c.getTargetHpPercentage());
            }).count();

        tryRemoveBuffs();
        if (challenge.isSuccess()) {
            getTowerData().updateFloorRecord(stars); // will not update if the current result is not better than recorded
            // record currently team energy level
            getCurTempEnergyMap().update(this.player.getTeamManager().getActiveTeam());
            updateNextLevel();
            notifyCurRecordChange();
            notifyFloorChange(getCurFloorRecordInfo());
        } else {
            removeCurrentLevelBuff();
            getTowerData().setUpperPart(true); // always starts from upper half if player leaves or fails challenge
        }

        return stars;
    }

    private int getNextLevelId() {
        return Optional.ofNullable(GameData.getTowerFloorDataMap().get(getCurrentFloorId()))
            .map(TowerFloorData::getLevelGroupId).map(floorDataGroupId -> GameData.getTowerLevelDataMap().values().stream()
                .filter(levelData -> levelData.getLevelGroupId() == floorDataGroupId).map(TowerLevelData::getLevelIndex)
                .filter(levelIndex -> levelIndex > getCurrentLevelIndex()).findFirst())
            .flatMap(innerOption -> innerOption).orElse(0);
    }

    /**
     * Check there is next level (return false when player reached chamber 3 of current floor),
     * should go to next floor at this point
     * */
    public boolean hasNextLevel() {
        return getNextLevelId() > 0;
    }

    public int getNextFloorId() {
        return getTowerSystem().getNextFloorId(getTowerData().getCurrentFloorId());
    }

    /**
     * Check there is next floor (return false when player reached floor 12)
     * */
    public boolean hasNextFloor() {
        return getNextFloorId() > 0;
    }

    /**
     * Check if player can enter floor 9
     * */
    public boolean canEnterScheduleFloor() {
        return getRecordMap().values().stream().filter(record -> record.getStarCount() >= STAR_COUNT_TO_UNLOCK_SCHEDULE_FLOOR)
            .anyMatch(record -> record.getFloorId() == getTowerSystem().getLastEntranceFloor());
    }

    /**
     * Change to second team for lower half of abyss, triggered by scripts
     * */
    public void mirrorTeamSetUp(int teamId) {
        getCurTempEnergyMap().update(this.player.getTeamManager().getActiveTeam());
        getTowerData().setUpperPart(false);
        rebuildAndUseTeam(teamId);
        this.player.sendPacket(new PacketTowerMiddleLevelChangeTeamNotify());
    }

    /**
     * Get start reward, or namely Star bounty
     * */
    public boolean getStarReward(int floorId) {
        val floorData = GameData.getTowerFloorDataMap().get(floorId);
        if (floorData == null) return false;

        val recordInfo = getRecordMap().get(floorData.getFloorIndex());
        if (recordInfo == null) return false;

        val rewardIds = recordInfo.getPassedLevelMap().keySet().stream().parallel()
            .map(integer -> GameData.getTowerLevelDataMap().get(integer.intValue()))
            .filter(Objects::nonNull).map(TowerLevelData::getLevelIndex)
            .map(levelIndex -> GameData.getTowerRewardData(levelIndex, floorData.getFloorIndex()))
            .filter(Objects::nonNull).map(rewardData -> rewardData.getStarRewardsByStarCount(recordInfo.getStarCount()))
            .flatMap(List::stream).filter(rewardId -> !recordInfo.getReceivedStarBounty().contains(rewardId)).distinct().toList();

        val rewardItems = rewardIds.stream().parallel().map(rewardId -> GameData.getRewardDataMap().get(rewardId.intValue()))
            .filter(Objects::nonNull).map(RewardData::getRewardItemList).flatMap(List::stream)
            .collect(Collectors.toMap(ItemParamData::getItemId, ItemParamData::getItemCount, Integer::sum))
            .entrySet().stream().map(e -> new GameItem(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(GameItem::getItemId)).toList();

        if (!rewardItems.isEmpty()) {
            recordInfo.onGetReward(rewardIds);
            notifyFloorChange(recordInfo);
            this.player.getInventory().addItems(rewardItems, ActionReason.TowerFloorStarReward);
        }

        return !rewardItems.isEmpty();
    }
}
