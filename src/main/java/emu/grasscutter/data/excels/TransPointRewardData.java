package emu.grasscutter.data.excels;

import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@ResourceType(name = "TransPointRewardConfigData.json")
@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class TransPointRewardData extends GameResource {
    int sceneId;
    int pointId;
    int rewardId;
    int[] groupIdVec;

    @Override
    public int getId() {
        return (this.sceneId << 16) + this.pointId;
    }
}
