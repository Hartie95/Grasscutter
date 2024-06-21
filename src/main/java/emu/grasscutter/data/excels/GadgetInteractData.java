package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.Getter;

@ResourceType(name = "GadgetInteractExcelConfigData.json")
@Getter
public class GadgetInteractData extends GameResource {
    private int interactId;
    private InteractActionType actionType;
    private int param1;
    private GadgetInteractAction[] actionList;
    private GadgetInteractCostItem[] costItems;
    private long uiTitleTextMapHash;
    private long uiDescTextMapHash;
    private GadgetInteractCond[] condList;
    private Boolean isGuestInteract;
    private Boolean isMpModeInteract;

    public enum InteractActionType {
        INTERACT_ACTION_NONE,
        INTERACT_ACTION_STATE,
        INTERACT_ACTION_SET_GADGET_CHAIN_BUFF,
        INTERACT_ACTION_UNLOCK_SPECIAL_TRANSPORT_POINT,
        INTERACT_ACTION_CONSUME_REGIONAL_PLAY_VAR
    }

    public enum InteractCondType {
        INTERACT_COND_NONE,
        INTERACT_COND_WIDGET_ON,
        INTERACT_COND_HAS_ITEM,
        INTERACT_COND_REGIONAL_PLAY_VAR_GREATER_THAN,
        INTERACT_COND_OFFERING_LEVEL
    }

    @Data
    public static class GadgetInteractAction {
        private InteractActionType actionType;
        private int[] param;
    }

    @Data
    public static class GadgetInteractCostItem {
        private int id;
        private int count;
    }

    @Data
    public static class GadgetInteractCond {
        private InteractCondType condType;
        private String[] param;
    }

    @Override
    public int getId() {
        return interactId;
    }
}
