package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.common.ItemParamData;
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
@ResourceType(name = {"HomeWorldLimitShopExcelConfigData.json"})
public class HomeWorldLimitShopData extends GameResource {
    private int goodsId;
    private int itemID;
    private int poolID;
    private List<HomeLimitedShopBuyGoodCond> cond;
    private int buyLimit;
    private List<ItemParamData> costItems;
    private int weight;

    public class HomeLimitedShopBuyGoodCond {
        @SerializedName(value="type", alternate={"HBINOLKJLGI"})
        private String type;
        @SerializedName(value="param", alternate={"EAGGPIMGBGH"})
        private List<Integer> param;
    }

    @Override
    public int getId() {
        return this.goodsId;
    }

    public void onLoad() {
        this.costItems = this.costItems.stream().filter(x -> x != null).toList();
    }
}
