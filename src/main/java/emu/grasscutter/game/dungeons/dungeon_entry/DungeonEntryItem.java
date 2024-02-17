package emu.grasscutter.game.dungeons.dungeon_entry;

import dev.morphia.annotations.Entity;
import emu.grasscutter.GameConstants;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.DungeonRosterData;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.val;
import org.anime_game_servers.game_data_models.gi.data.dungeon.DungeonData;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


/**
 * Holds dungeon entry information, including weekly boss discount information
 * */
@Entity
@Getter
public class DungeonEntryItem {
    private final Set<Integer> passedDungeons = new TreeSet<>();
    /**
     * entry: [dungeon serial id, boss record]
     * */
    private final Map<Integer, WeeklyBossRecord> bossRecordMap = new Int2ObjectOpenHashMap<>();

    public void addDungeon(int dungeonId) {
        this.passedDungeons.add(dungeonId);
    }

    public void updateWeeklyBossInfo(int dungeonSerialId) {
        val selectedDungeon = this.bossRecordMap.get(dungeonSerialId);
        if (selectedDungeon == null || !selectedDungeon.canTakeReward()) return;

        selectedDungeon.update();
        this.bossRecordMap.values().forEach(WeeklyBossRecord::sync);
    }

    public void checkForNewBoss() {
        if (!this.bossRecordMap.keySet().containsAll(GameData.getDungeonSerialDataMap().keySet())) {
            GameData.getDungeonDataMap().values().stream().filter(data -> data.getSerialId() > 0)
                .map(data -> GameData.getDungeonSerialDataMap().get(data.getSerialId())).filter(Objects::nonNull)
                .forEach(serialData -> this.bossRecordMap.putIfAbsent(
                    serialData.getId(), WeeklyBossRecord.create(serialData)));
        }
    }

    public void buildRosterDungeon() {
        GameData.getDungeonRosterDataMap().values().stream().filter(DungeonRosterData::nowIsAfterOpenTime)
            .forEach(data -> data.getRosterPool().stream().map(DungeonRosterData.RosterPool::getDungeonList)
                .flatMapToInt(Arrays::stream).mapToObj(GameData.getDungeonDataMap()::get).map(DungeonData::getSerialId)
                .map(this.bossRecordMap::get).filter(Objects::nonNull).filter(record -> record.getRosterCycleRecord() == null)
                .forEach(record -> record.buildRosterRecord(data.getId())));
    }

    public void resetWeeklyBoss(){
        this.bossRecordMap.values().stream().filter(record ->  shouldReset(
                ZonedDateTime.now(GameConstants.ZONE_ID), toZonedDateTime(record.getLastCycledTime())))
            .forEach(WeeklyBossRecord::reset);
    }

    private static boolean shouldReset(ZonedDateTime currentDateTime, ZonedDateTime lastRefreshDateTime){
        val lastMonday = currentDateTime.with(TemporalAdjusters.previous(GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_REFRESH_DAY))
            .with(LocalTime.of(GameConstants.REFRESH_HOUR, 0, 0));
        return lastRefreshDateTime == null || Duration.between(lastRefreshDateTime, lastMonday)
            .toDays() >= GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_REFRESH_DAY_INTERVAL;
    }

    /**
     * Convert string to ZoneDateTime,
     * string should be in this format: yyyy-MM-dd HH:mm:ss
     * */
    private static ZonedDateTime toZonedDateTime(String timeStr) {
        return ZonedDateTime.parse(timeStr, GameConstants.TIME_FORMATTER_FULL.withZone(GameConstants.ZONE_ID));
    }

    static String getLastRefreshTimeStr(){
        return ZonedDateTime.now(GameConstants.ZONE_ID)
            .with(TemporalAdjusters.previous(GameConstants.WEEKLY_BOSS_RESIN_DISCOUNT_REFRESH_DAY))
            .with(LocalTime.of(GameConstants.REFRESH_HOUR, 0, 0)).format(GameConstants.TIME_FORMATTER_FULL);
    }
}
