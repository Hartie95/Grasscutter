package emu.grasscutter.data.excels;

import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@ResourceType(name = "CityLevelupConfigData.json")
@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CityLevelUpData extends GameResource {
    int sceneId;
    int cityId;
    int level;
    ItemParamData consumeItem;
    int rewardID;
    List<LevelUpAction> actionVec;

    @Override
    public int getId() {
        return this.rewardID; // only reward id is unique
    }

    @Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
    public class LevelUpAction {
        String type;
        int[] param1Vec;
        int[] param2Vec;
    }
}
