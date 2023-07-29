package emu.grasscutter.data.excels;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.data.common.BaseBlossomROSData;
import emu.grasscutter.game.managers.blossom.enums.BlossomClientShowType;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshCondType;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ResourceType(name = "BlossomRefreshExcelConfigData.json")
@EqualsAndHashCode(callSuper = false)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlossomRefreshData extends GameResource implements BaseBlossomROSData {
    @Getter(onMethod = @__(@Override))
    int id;
    String clientShowType;  // BLOSSOM_SHOWTYPE_CHALLENGE, BLOSSOM_SHOWTYPE_NPCTALK

    // Refresh details
    @SerializedName(value="refreshType")
    String refreshTypeString;  // Leyline blossoms, magical ore outcrops
    int refreshCount;  // Number of entries to spawn at refresh (1 for each leyline type for each city, 4 for magical ore for each city)
    String refreshTime;  // Server time-of-day to refresh at
    List<RefreshCond> refreshCondVec;  // AR requirements etc.

    int cityId;
    int blossomChestId;  // 1 for mora, 2 for exp
    List<Drop> dropVec;

    // Unknown details
    int reviseLevel;
    int campUpdateNeedCount;  // Always 1 if specified

    transient BlossomClientShowType blossomClientShowType;

    transient BlossomRefreshType refreshType;

    @Override
    public void onLoad() {
        this.blossomClientShowType = BlossomClientShowType.getTypeByName(this.clientShowType);
        this.refreshType = BlossomRefreshType.getTypeByName(this.refreshTypeString);
        this.refreshCondVec = this.refreshCondVec.stream().filter(cond -> !cond.getParam().isEmpty()).toList();
        this.refreshCondVec.forEach(RefreshCond::onLoad);
    }

    @Override
    public int getRefreshId() {
        return getId();
    }

    @Override
    public int getRewardId(int worldLevel) {
        return getDropVec().get(worldLevel).getPreviewReward();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Data
    public static class Drop {
        int dropId;
        int previewReward;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Data
    public static class RefreshCond {
        String type;
        List<Integer> param;

        transient BlossomRefreshCondType refreshCondType;

        public void onLoad(){
            this.param = this.param.stream().filter(x -> (x != null) && (x != 0)).toList();
            this.refreshCondType = BlossomRefreshCondType.getTypeByName(this.type);
        }
    }
}
