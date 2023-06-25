package emu.grasscutter.data.excels;

import java.util.List;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.managers.blossom.enums.BlossomClientShowType;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshCondType;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@ResourceType(name = "BlossomRefreshExcelConfigData.json")
@Getter
public class BlossomRefreshData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int id;
    // Map details
    // private long nameTextMapHash;
    // private long descTextMapHash;
    // private String icon;
    private String clientShowType;  // BLOSSOM_SHOWTYPE_CHALLENGE, BLOSSOM_SHOWTYPE_NPCTALK

    // Refresh details
    private String refreshType;  // Leyline blossoms, magical ore outcrops
    private int refreshCount;  // Number of entries to spawn at refresh (1 for each leyline type for each city, 4 for magical ore for each city)
    private String refreshTime;  // Server time-of-day to refresh at
    private List<RefreshCond> refreshCondVec;  // AR requirements etc.

    private int cityId;
    private int blossomChestId;  // 1 for mora, 2 for exp
    private List<Drop> dropVec;

    // Unknown details
    @Getter private int reviseLevel;
    @Getter private int campUpdateNeedCount;  // Always 1 if specified

    private transient BlossomClientShowType blossomClientShowType;

    private transient BlossomRefreshType blossomRefreshType;

    @Override
    public void onLoad() {
        this.blossomClientShowType = BlossomClientShowType.getTypeByName(this.clientShowType);
        this.blossomRefreshType = BlossomRefreshType.getTypeByName(this.refreshType);
        this.refreshCondVec = this.refreshCondVec.stream().filter(cond -> !cond.getParam().isEmpty()).toList();
        this.refreshCondVec.forEach(RefreshCond::onLoad);
    }

    @Getter
    public static class Drop {
        int dropId;
        int previewReward;
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
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
