package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper=false)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ResourceType(name = {"HomeworldModuleExcelConfigData.json"})
public class HomeWorldModuleData extends GameResource {
    private int Id;
    private boolean isFree;
    private int worldSceneId;
    private int defaultRoomSceneId;
    private List<Integer> optionalRoomSceneIdVec;
    // properties might be useful but unused yet
    // "DNIILOMHHEI": 1478493845,
    // "moduleNameTextMapHash": 1843410581,
    // "moduleDescTextMapHash": 182060131,
    // "region": [
    //     "Map_Homeworld_Exterior_Above_001",
    //     "Map_Homeworld_Exterior_Above_002",
    //     "Map_Homeworld_Exterior_Above_003",
    //     "Map_Homeworld_Exterior_Above_004"
    // ],
    // "regionPointPos": [
    //     "825.7461,0,123.5733",
    //     "761.4891,0,364.6011",
    //     "525.3538,0,589.5408",
    //     "218.1062,0,825.1384"
    // ],
    // "smallImageAddr": "UI_HomeworldModule_2",
    // "bigImageAddr": "UI_HomeworldModule_2_Pic"

    @Override
    public int getId() {
        return this.Id;
    }
}
