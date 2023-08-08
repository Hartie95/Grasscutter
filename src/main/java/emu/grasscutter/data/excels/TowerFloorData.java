package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@ResourceType(name = "TowerFloorExcelConfigData.json")
@Data
@EqualsAndHashCode(callSuper = false)
public class TowerFloorData extends GameResource {
    @Getter(onMethod = @__(@Override))
    @SerializedName(value = "floorId")
    private int id;
    private int floorIndex;
    private int levelGroupId;
    private int overrideMonsterLevel;
    private int teamNum;
    private int floorLevelConfigId;
}
