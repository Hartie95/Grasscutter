package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.Getter;

@ResourceType(name = "GadgetInteractExcelConfigData.json")
@Getter
public class GadgetInteractData extends GameResource {
    private int interactId;
    private String actionType;
    private int param1;
    private GadgetInteractActionList[] actionList;
    private GadgetInteractCostItems[] costItems;
    private long uiTitleTextMapHash;
    private long uiDescTextMapHash;
    private GadgetInteractCondList[] condList;
    private Boolean consumeItemNum;

    @Data
    public static class GadgetInteractActionList {
        private String actionType;
        private int[] param;
    }

    @Data
    public static class GadgetInteractCostItems {
        private int id;
        private int count;
    }

    @Data
    public static class GadgetInteractCondList {
        private String condType;
        private String[] param;
    }

    public int getId() {
        return interactId;
    }
}
