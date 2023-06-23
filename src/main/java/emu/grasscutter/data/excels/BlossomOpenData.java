package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Getter;

@ResourceType(name = "BlossomGroupsExcelConfigData.json")
@Getter
public class BlossomOpenData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int cityId;
    private int openLevel;
}
