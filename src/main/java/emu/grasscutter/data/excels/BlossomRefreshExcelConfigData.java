package emu.grasscutter.data.excels;

import java.util.List;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.managers.blossom.enums.BlossomClientShowType;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshCondType;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import lombok.Getter;

@ResourceType(name = "BlossomRefreshExcelConfigData.json")
@Getter
public class BlossomRefreshExcelConfigData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int id;
    // Map details
    // private long nameTextMapHash;
    // private long descTextMapHash;
    // private String icon;
    private BlossomClientShowType clientShowType;  // BLOSSOM_SHOWTYPE_CHALLENGE, BLOSSOM_SHOWTYPE_NPCTALK

    // Refresh details
    private BlossomRefreshType refreshType;  // Leyline blossoms, magical ore outcrops
    private int refreshCount;  // Number of entries to spawn at refresh (1 for each leyline type for each city, 4 for magical ore for each city)
    private String refreshTime;  // Server time-of-day to refresh at
    private List<RefreshCond> refreshCondVec;  // AR requirements etc.

    private int cityId;
    private int blossomChestId;  // 1 for mora, 2 for exp
    private List<Drop> dropVec;

    // Unknown details
     @Getter private int reviseLevel;
     @Getter private int campUpdateNeedCount;  // Always 1 if specified

    @Getter
    public static class Drop {
        int dropId;
        int previewReward;
    }

    @Getter
    public static class RefreshCond {
        BlossomRefreshCondType type;
        List<Integer> param;
    }
}
