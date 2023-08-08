package emu.grasscutter.data.common;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;


/**
 * Records the entry and exit trans pos and rot for dungeon/domains
 * */
@Getter
@ToString
@Builder(builderMethodName = "of", builderClassName = "Builder", setterPrefix = "set")
public class DungeonEntries {
    private final int dungeonId;
    private final PointData entryPoint;
    private final PointData exitPoint;

    public static DungeonEntries create(int dungeonId, PointData pointData, Map<Integer, PointData> exitPointMap) {
        return DungeonEntries.of()
            .setDungeonId(dungeonId)
            .setEntryPoint(pointData)
            .setExitPoint(exitPointMap.getOrDefault((pointData.getSceneId() << 16) + pointData.getId(), pointData))
            .build();
    }

    public int getEntryKey() {
        return (this.entryPoint.getSceneId() << 16) + this.entryPoint.getId();
    }
}
