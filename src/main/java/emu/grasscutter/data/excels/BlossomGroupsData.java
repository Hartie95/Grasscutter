package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Getter;

import java.util.List;

@ResourceType(name = "BlossomGroupsExcelConfigData.json")
@Getter
public class BlossomGroupsData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int id;
    private int cityId;
    private int sectionId;
    private List<Integer> refreshTypeVec;
    private List<Integer> newGroupVec;
    private List<Integer> decorateGroupVec;
    private List<Integer> nextCampIdVec;
    private boolean isSafe;
    private boolean isInitialRefresh;
    private int finishProgress;
    private int limitLevel;
    private int fightRadius;
    private int remindRadius;

}
