package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Getter;

@ResourceType(name = "BlossomOpenExcelConfigData.json")
@Getter
public class BlossomOpenData extends GameResource {

    private int cityId;
    private int openLevel;

    @Override
    public int getId() {
        return this.cityId;
    }
}
