package emu.grasscutter.game.tower;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.morphia.annotations.Entity;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.TowerFloorData;
import emu.grasscutter.data.excels.TowerLevelData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.TowerCurLevelRecordOuterClass.TowerCurLevelRecord;
import emu.grasscutter.net.proto.TowerFloorRecordOuterClass.TowerFloorRecord;
import emu.grasscutter.net.proto.TowerLevelRecordOuterClass.TowerLevelRecord;
import emu.grasscutter.net.proto.TowerMonthlyBriefOuterClass.TowerMonthlyBrief;
import emu.grasscutter.net.proto.TowerTeamOuterClass.TowerTeam;
import emu.grasscutter.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.val;


/**
 * Spiral abyss information
 * domain in form of 12-1, 12-2, 12-3 for example
 * 12 is floor id, 1, 2 and 3 is level index
 * */
@Entity @Getter
public class TowerData{
    /**
     * the floor players chose
     */
    private int currentFloorId;
    /**
     * Used to get game data
     * */
    private int currentLevelId;
    /**
     * the level player currently in
     * */
    private int currentLevelIndex;
    /**
     * whether it is upper or lower half of the level
     * */
    @Setter private boolean isUpperPart;
    /**
     * player's tower team
     * */
    private final List<TowerTeamInfo> teamOnHold = new ArrayList<>();
    /**
     * avatar's energy usage
     * entry: [teamIndex, energy charge count]
     * */
    private final Map<Integer, TowerTeamEnergyInfo> teamEnergyMap = new Int2ObjectOpenHashMap<>();
    /**
     * player selected buffs
     * entry: [floorIndex, TowerBuffId]
     * */
    private final Map<Integer, Integer> towerBuffs = new Int2IntArrayMap();
    /**
     * entry: [floorIndex, Record]
     */
    private final Map<Integer, TowerFloorRecordInfo> recordMap = new Int2ObjectOpenHashMap<>();
    /**
     * Holds current scheduleId
     * */
    private int scheduleId;
    /**
     * Shows best progress of previously scheduled tower
     */
    private TowerMonthlyBriefInfo monthlyBriefInfo;

    public void refreshCurLevelRecord(List<TowerTeam> newTowerTeam, Player player, TowerFloorData floorData) {
        this.isUpperPart = true;
        this.towerBuffs.clear();
        addNewTeam(newTowerTeam, player);
        this.currentFloorId = floorData.getId();
        // it is safe to assume that level index is 1 whenever this function is occur
        this.currentLevelIndex = 1;
        this.currentLevelId = GameData.getTowerLevelDataMap().values().stream()
            .filter(x -> x.getLevelGroupId() == floorData.getLevelGroupId() && x.getLevelIndex() == 1)
            .findFirst()
            .map(TowerLevelData::getId)
            .orElse(0);
    }

    public void reset() {
        this.isUpperPart = false;
        this.teamOnHold.clear();
        this.teamEnergyMap.clear();
        this.towerBuffs.clear();
        this.currentFloorId = 0;
        this.currentLevelIndex = 0;
        this.currentLevelId = 0;
    }

    public void addNewTeam(List<TowerTeam> newTowerTeam, Player player) {
        this.teamOnHold.clear();
        this.teamOnHold.addAll(newTowerTeam.stream()
            .map(towerTeam -> TowerTeamInfo.create(towerTeam, player))
            .toList());
        this.teamEnergyMap.clear();
        // team id starts from 1
        this.teamEnergyMap.putAll(newTowerTeam.stream().collect(Collectors.toMap(
            TowerTeam::getTowerTeamId, towerTeam -> TowerTeamEnergyInfo.create(towerTeam, player))));
    }

    public void updateEnergyMap(Map<Integer, TowerTeamEnergyInfo> newTeamInfo) {
        this.teamEnergyMap.clear();
        this.teamEnergyMap.putAll(newTeamInfo);
    }

    // it is safe to assume there is only two teams in abyss
    public int getTeamIndex() {
        return this.isUpperPart ? 0 : 1;
    }

    public List<Integer> getRandomBuffs() {
        return Optional.ofNullable(GameData.getTowerLevelDataMap().get(this.currentLevelId))
            .map(TowerLevelData::getTowerBuffConfigFloorList).stream()
            .flatMap(List::stream)
            .filter(levelBuffList -> !levelBuffList.getConfigList().isEmpty())
            .map(levelBuffList -> {
                val configList = levelBuffList.getConfigList().stream().filter(buff -> !this.towerBuffs.containsValue(buff.getId())).toList();
                return Utils.drawRandomListElement(configList.stream().map(TowerLevelData.TowerBuffConfig::getId).toList(),
                    configList.stream().map(TowerLevelData.TowerBuffConfig::getWeight).toList());
            }).toList();
    }

    public void updateFloorRecord(int star) {
        Optional.ofNullable(GameData.getTowerFloorDataMap().get(this.currentFloorId)).ifPresent(floorData ->
            this.recordMap.computeIfAbsent(floorData.getFloorIndex(), f -> TowerFloorRecordInfo.create(floorData.getId()))
                .update(this.currentLevelId, this.currentLevelIndex, star));
    }

    public void syncToNextLevel(int levelId) {
        val levelData = Optional.ofNullable(GameData.getTowerLevelDataMap().get(levelId));
        this.currentLevelIndex = levelData.map(TowerLevelData::getLevelIndex).orElse(0);
        this.currentLevelId = levelData.map(TowerLevelData::getId).orElse(0);
        this.isUpperPart = true;
    }

    public void startNewSchedule(int scheduleId) {
        // it should record the old schedule information
        val bestFloorIndex = this.recordMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        val bestFloorRecord = Optional.ofNullable(this.recordMap.get(bestFloorIndex));
        getMonthBriefInfo().update(
            this.scheduleId,
            bestFloorIndex,
            bestFloorRecord.map(TowerFloorRecordInfo::getBestLevelIndex).orElse(0),
            bestFloorRecord.map(TowerFloorRecordInfo::getStarCount).orElse(0));

        // update/reset new information
        this.scheduleId = scheduleId;
        // TODO maybe read of data excels to make less hard coded
        this.recordMap.entrySet().removeIf(entry -> entry.getKey() >= 9 && entry.getKey() <= 12);
    }

    public List<TowerTeam> towerTeamProto(Player player){
        return this.teamOnHold.stream().map(t -> t.toProto(player)).toList();
    }

    public TowerCurLevelRecord toProto(Player player) {
        return TowerCurLevelRecord.newBuilder()
            .setCurFloorId(this.currentFloorId)
            .setCurLevelIndex(this.currentLevelIndex)
            .addAllTowerTeamList(towerTeamProto(player))
            .addAllBuffIdList(this.towerBuffs.values().stream().toList())
            .setUnk2700CBPNPEBMPOH(this.isUpperPart)
            .setIsEmpty(this.teamOnHold.isEmpty())
            .build();
    }

    public List<TowerFloorRecord> getTowerRecordProtoList() {
        return this.recordMap.values().stream()
            .map(TowerFloorRecordInfo::toProto)
            .sorted(Comparator.comparing(TowerFloorRecord::getFloorId))
            .toList();
    }

    public TowerMonthlyBriefInfo getMonthBriefInfo() {
        if (this.monthlyBriefInfo == null) {
            this.monthlyBriefInfo = TowerMonthlyBriefInfo.create(this.scheduleId);
        }
        return this.monthlyBriefInfo;
    }

    @Entity @Getter @Builder(builderMethodName = "of")
    public static class TowerTeamInfo {
        private int teamId;
        private List<Integer> avatarIds;

        public static TowerTeamInfo create(TowerTeam newTeam, Player player) {
            return TowerTeamInfo.of()
                .teamId(newTeam.getTowerTeamId())
                .avatarIds(newTeam.getAvatarGuidListList().stream()
                    .map(player.getAvatars()::getAvatarByGuid).filter(Objects::nonNull)
                    .map(Avatar::getAvatarId).toList())
                .build();
        }

        public TowerTeam toProto(Player player) {
            return TowerTeam.newBuilder()
                .setTowerTeamId(this.teamId)
                .addAllAvatarGuidList(this.avatarIds.stream()
                    .map(player.getAvatars()::getAvatarById)
                    .map(Avatar::getGuid)
                    .toList())
                .build();
        }
    }

    @Entity @Getter @Builder(builderMethodName = "of")
    public static class TowerTeamEnergyInfo {
        /**
         * avatar's energy usage
         * entry: [avatarId, energy charge value]
         * */
        private Map<Integer, Float> avatarEnergyMap;

        public static TowerTeamEnergyInfo create() {
            return TowerTeamEnergyInfo.of()
                .avatarEnergyMap(new Int2ObjectOpenHashMap<>())
                .build();
        }

        private static TowerTeamEnergyInfo create(TowerTeam team, Player pLayer) {
            return TowerTeamEnergyInfo.of()
                .avatarEnergyMap(team.getAvatarGuidListList().stream().map(pLayer.getAvatars()::getAvatarByGuid)
                    .collect(Collectors.toMap(Avatar::getAvatarId, f -> 0f)))
                .build();
        }

        public void update(List<EntityAvatar> entityAvatars) {
            entityAvatars.stream().map(EntityAvatar::getAvatar).forEach(avatar ->
                this.avatarEnergyMap.put(avatar.getAvatarId(), avatar.getCurrentEnergy()));
        }
    }

    @Entity @Getter @Builder(builderMethodName = "of")
    public static class TowerLevelRecordInfo {
        private int levelId;
        private Set<Integer> satisfiedCondList;
        @Setter private boolean receivedFirstPassReward;

        public static TowerLevelRecordInfo create(int levelId) {
            return TowerLevelRecordInfo.of()
                .levelId(levelId)
                .satisfiedCondList(new TreeSet<>())
                .receivedFirstPassReward(false)
                .build();
        }

        public void update(int star) {
            IntStream.rangeClosed(1, star).forEach(this.satisfiedCondList::add);
        }

        public TowerLevelRecord toProto() {
            return TowerLevelRecord.newBuilder()
                .setLevelId(this.levelId)
                .addAllSatisfiedCondList(this.satisfiedCondList)
                .build();
        }
    }

    @Entity @Getter @Builder(builderMethodName = "of")
    public static class TowerFloorRecordInfo {
        /**
         * FloorId in config
         */
        private int floorId;
        /**
         * LevelId - Stars
         */
        private Map<Integer, Integer> passedLevelMap;
        /**
         * Satisfied conditions to get stars
         * entry: [levelIndex from 0 to 2, passed cond]
         * */
        private Map<Integer, TowerLevelRecordInfo> passedLevelRecordMap;
        private Set<Integer> receivedStarBounty;
        /**
         * Number of star bounty claimed
         * */
        private int floorStarRewardProgress;

        public static TowerFloorRecordInfo create(int floorId) {
            return TowerFloorRecordInfo.of()
                .floorId(floorId)
                .passedLevelMap(new HashMap<>())
                .passedLevelRecordMap(new HashMap<>())
                .build();
        }

        public int getStarCount() {
            return passedLevelMap.values().stream().mapToInt(Integer::intValue).sum();
        }

        public boolean update(int levelId, int levelIndex, int star) {
            int oldStarCount = this.passedLevelMap.getOrDefault(levelId, 0);
            if (oldStarCount >= star) return false;

            this.passedLevelMap.put(levelId, star);
            this.passedLevelRecordMap.computeIfAbsent(levelIndex-1, f -> TowerLevelRecordInfo.create(levelId)).update(star);
            return true;
        }

        public void onGetReward(List<Integer> rewardIds) {
            this.receivedStarBounty.addAll(rewardIds);
            this.floorStarRewardProgress = this.receivedStarBounty.size();
        }

        public Set<Integer> getReceivedStarBounty() {
            if (this.receivedStarBounty == null) {
                this.receivedStarBounty = new HashSet<>();
            }
            return this.receivedStarBounty;
        }

        private int getBestLevelIndex() {
            return this.passedLevelMap.keySet().stream().mapToInt(Integer::intValue).map(key -> key + 1).max().orElse(0);
        }

        public TowerFloorRecord toProto() {
            return TowerFloorRecord.newBuilder()
                .setFloorId(this.floorId)
                .setFloorStarRewardProgress(this.floorStarRewardProgress)
                .putAllPassedLevelMap(this.passedLevelMap)
                .addAllPassedLevelRecordList(this.passedLevelRecordMap.values().stream()
                    .map(TowerLevelRecordInfo::toProto)
                    .sorted(Comparator.comparing(TowerLevelRecord::getLevelId)).toList())
                .build();
        }
    }

    @Entity @Getter @Builder(builderMethodName = "of")
    public static class TowerMonthlyBriefInfo {
        private int towerScheduleId;
        private int bestFloorIndex;
        private int bestLevelIndex;
        private int totalStartCount;

        public static TowerMonthlyBriefInfo create(int scheduleId) {
            return TowerMonthlyBriefInfo.of()
                .towerScheduleId(scheduleId)
                .build();
        }

        public void update(int scheduleId, int bestFloorIndex, int bestLevelIndex, int totalStartCount) {
            this.towerScheduleId = scheduleId;
            this.bestFloorIndex = bestFloorIndex;
            this.bestLevelIndex = bestLevelIndex;
            this.totalStartCount = totalStartCount;
        }

        public TowerMonthlyBrief toProto() {
            return TowerMonthlyBrief.newBuilder()
                .setTowerScheduleId(this.towerScheduleId)
                .setBestFloorIndex(this.bestFloorIndex)
                .setBestLevelIndex(this.bestLevelIndex)
                .setTotalStarCount(this.totalStartCount)
                .build();
        }
    }
}
