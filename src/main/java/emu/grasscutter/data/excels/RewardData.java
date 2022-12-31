package emu.grasscutter.data.excels;

import java.util.List;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import emu.grasscutter.data.common.ItemParamData;

@ResourceType(name = "RewardExcelConfigData.json")
@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardData extends GameResource {
    int rewardId;
    int hcoin;
    int playerExp;
    List<ItemParamData> rewardItemList;

    @Override
	public int getId() {
		return rewardId;
	}

    @Override
    public void onLoad() {
    	rewardItemList = rewardItemList.stream().filter(i -> i.getId() > 0).toList();
    }
}
