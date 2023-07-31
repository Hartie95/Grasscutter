package emu.grasscutter.data.common;

import com.google.gson.annotations.SerializedName;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.utils.Position;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings(value = "SpellCheckingInspection")
public class PointData {
    private int id;
    @SerializedName(value="type", alternate={"$type"})
    private String type;
    private Position tranPos;
    private Position pos;
    private Position rot;
    private Position size;

    @SerializedName(value="dungeonIds", alternate={"JHHFPGJNMIN"})
    private int[] dungeonIds;
    @SerializedName(value="dungeonRandomList", alternate={"OIBKFJNBLHO"})
    private int[] dungeonRandomList;
    private int[] dungeonRosterList;
    @SerializedName(value="groupIDs", alternate={"HFOBOOHKBGF"})
    private int[] groupIds;
    @SerializedName(value="tranSceneId", alternate={"JHBICGBAPIH"})
    private int tranSceneId;

    public void updateDailyDungeon() {
        if (this.dungeonRandomList != null && this.dungeonRandomList.length > 0) {
            this.dungeonIds = Arrays.stream(this.dungeonRandomList)
                .mapToObj(GameData.getDailyDungeonDataMap()::get).filter(Objects::nonNull)
                .map(data -> data.getDungeonsByDay(Grasscutter.getCurrentDayOfWeek()))
                .flatMapToInt(Arrays::stream)
                .filter(GameData.getDungeonDataMap()::containsKey)
                .toArray();
        }
    }
}
