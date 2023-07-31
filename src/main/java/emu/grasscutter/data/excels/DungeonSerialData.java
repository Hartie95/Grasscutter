package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@ResourceType(name = "DungeonSerialConfigData.json")
@Data
@EqualsAndHashCode(callSuper = false)
public class DungeonSerialData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int id;
    private int maxTakeNum;
    private int takeCost;
}
