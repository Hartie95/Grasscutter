package emu.grasscutter.game.player;

import dev.morphia.annotations.Entity;
import emu.grasscutter.GameConstants;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.DungeonSerialData;
import emu.grasscutter.net.proto.WeeklyBossResinDiscountInfoOuterClass.WeeklyBossResinDiscountInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Builder;
import lombok.Getter;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Entity
public class DungeonEntryItem {
    @Getter private final Set<Integer> passedDungeons;
    /**
     * entry: [dungeon serial id, boss record]
     * */
    @Getter private final Map<Integer, WeeklyBossRecord> bossRecordMap;

    public DungeonEntryItem() {
        this.passedDungeons = new TreeSet<>();
        this.bossRecordMap = new Int2ObjectOpenHashMap<>();
    }

    public void addDungeon(int dungeonId) {
        this.passedDungeons.add(dungeonId);
    }

    public void updateWeeklyBossInfo(int dungeonSerialId) {
        if (!this.bossRecordMap.containsKey(dungeonSerialId)) return;

        this.bossRecordMap.get(dungeonSerialId).update();
        this.bossRecordMap.values().forEach(WeeklyBossRecord::sync);
    }

    public void resetWeeklyBoss(){
        ZonedDateTime currentDateTime = ZonedDateTime.now(GameConstants.ZONE_ID);
        this.bossRecordMap.values().stream()
            .filter(record ->  shouldReset(currentDateTime, toZonedDateTime(record.getLastCycledTime())))
            .forEach(WeeklyBossRecord::reset);
    }

    private static boolean shouldReset(ZonedDateTime currentDateTime, ZonedDateTime lastRefreshDateTime){
        ZonedDateTime lastMonday = currentDateTime.with(TemporalAdjusters.previous(GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_REFRESH_DAY))
            .with(LocalTime.of(GameConstants.REFRESH_HOUR, 0, 0));
        return lastRefreshDateTime == null || Duration.between(lastRefreshDateTime, lastMonday)
            .toDays() >= GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_REFRESH_DAY_INTERVAL;
    }

    private static ZonedDateTime toZonedDateTime(String timeStr) {
        return ZonedDateTime.parse(timeStr, GameConstants.TIME_FORMATTER.withZone(GameConstants.ZONE_ID));
    }

    private static String getLastRefreshTimeStr(){
        return ZonedDateTime.now(GameConstants.ZONE_ID)
            .with(TemporalAdjusters.previous(GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_REFRESH_DAY))
            .with(LocalTime.of(GameConstants.REFRESH_HOUR, 0, 0)).format(GameConstants.TIME_FORMATTER);
    }

    @Entity
    @Builder(builderMethodName = "of")
    @Getter
    public static class RosterCycleRecord {
        private int rosterId;
        private int selectedPool;
        private String lastCycledTime;

        public static RosterCycleRecord create(int rosterId) {
            return RosterCycleRecord.of()
                .rosterId(rosterId)
                .lastCycledTime(getLastRefreshTimeStr())
                .selectedPool(0)
                .build();
        }

        public void reset() {
            Optional.ofNullable(GameData.getDungeonRosterDataMap().get(this.rosterId))
                .ifPresent(data -> this.selectedPool = data.getNextPool(this.selectedPool));
            this.lastCycledTime = getLastRefreshTimeStr();
        }
    }

    @Entity
    @Builder(builderMethodName = "of")
    @Getter
    public static class WeeklyBossRecord {
        private int serialId;
        private int discountNum;
        private int discountNumLimit;
        private int resinCost;
        private int originalResinCost;
        private String lastCycledTime;
        private int takeNum;
        private int maxTakeNumLimit;
        private RosterCycleRecord rosterCycleRecord;

        public static WeeklyBossRecord create(DungeonSerialData serialData) {
            return WeeklyBossRecord.of()
                .serialId(serialData.getId())
                .discountNum(0)
                .discountNumLimit(GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_COUNT)
                .originalResinCost(serialData.getTakeCost())
                .resinCost((int) (serialData.getTakeCost() * GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_VALUE))
                .takeNum(0)
                .maxTakeNumLimit(serialData.getMaxTakeNum())
                .lastCycledTime(getLastRefreshTimeStr())
                .build();
        }

        public RosterCycleRecord getRosterRecord(int rosterId){
            if (this.rosterCycleRecord == null) {
                this.rosterCycleRecord = RosterCycleRecord.create(rosterId);
            }
            return this.rosterCycleRecord;
        }

        public int getNextRefreshTime(){
            if (this.lastCycledTime == null || this.lastCycledTime.isBlank()) return 0;
            // Get the current date and time in the specified time zone
            ZonedDateTime now = ZonedDateTime.now(GameConstants.ZONE_ID);
            ZonedDateTime nextMonday = now.with(TemporalAdjusters.next(GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_REFRESH_DAY))
                .with(LocalTime.of(GameConstants.REFRESH_HOUR, 0, 0));
            return (int) Instant.now().plus(Duration.between(now, nextMonday)).getEpochSecond();
        }

        public void update() {
            this.takeNum += 1;
        }

        public void sync() {
            this.discountNum += 1;
        }

        public void reset() {
            this.discountNum = 0;
            this.takeNum = 0;
            this.lastCycledTime = getLastRefreshTimeStr();
            Optional.ofNullable(this.rosterCycleRecord).ifPresent(RosterCycleRecord::reset);
        }

        private int getResinCost() {
            return this.discountNum < this.discountNumLimit ? this.resinCost : this.originalResinCost;
        }

        public WeeklyBossResinDiscountInfo toProto() {
            return WeeklyBossResinDiscountInfo.newBuilder()
                .setDiscountNum(this.discountNum)
                .setDiscountNumLimit(this.discountNumLimit)
                .setResinCost(getResinCost())
                .setOriginalResinCost(this.originalResinCost)
                .build();
        }
    }
}
