package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;


@EqualsAndHashCode(callSuper=false)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ResourceType(name = {"HomeWorldComfortLevelExcelConfigData.json"})
public class HomeWorldComfortLevelData extends GameResource {
    private int levelID;
    private int comfort;
    private int homeCoinProduceRate;
    private int companionshipExpProduceRate;
    // "levelNameTextMapHash": 196277833,
    // "levelIconHashSuffix": 17080293789076112984

    @Override
    public int getId() {
        return this.comfort;
    }
}
