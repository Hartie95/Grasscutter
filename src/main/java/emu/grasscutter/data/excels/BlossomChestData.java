package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Getter;

@ResourceType(name = "BlossomChestExcelConfigData.json")
@Getter
public class BlossomChestData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int id;
    private int chestGadgetId;
    private int worldResin;
    private int resin;
    private String refreshType;
}
