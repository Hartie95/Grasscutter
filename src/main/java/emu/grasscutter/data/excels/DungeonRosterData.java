package emu.grasscutter.data.excels;


import emu.grasscutter.GameConstants;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ResourceType(name = "DungeonRosterConfigData.json")
@Data
@EqualsAndHashCode(callSuper = false)
public class DungeonRosterData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int id;
    private String openTimeStr;
    private int cycleTime; // in hours
    private String cycleType;
    private List<RosterPool> rosterPool;

    @Override
    public void onLoad() {
        this.rosterPool = this.rosterPool.stream()
            .filter(pool -> Optional.ofNullable(pool.dungeonList).filter(l -> l.length > 0).isPresent())
            .toList();
    }

    public boolean nowIsAfterOpenTime() {
        return ZonedDateTime.now(GameConstants.ZONE_ID).isAfter(ZonedDateTime.parse(
            this.openTimeStr, GameConstants.TIME_FORMATTER.withZone(GameConstants.ZONE_ID)));
    }

    public int getNextPool(int previousPool) {
        return (previousPool + 1) % this.rosterPool.size();
    }

    @Data
    public static class RosterPool {
        int[] dungeonList;
    }
}
