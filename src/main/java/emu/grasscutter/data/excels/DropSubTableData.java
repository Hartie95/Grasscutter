package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.data.ResourceType.LoadPriority;
import emu.grasscutter.data.common.DropItemData;
import lombok.Getter;

import java.util.List;

@ResourceType(name = "DropSubTableExcelConfigData.json", loadPriority = LoadPriority.HIGH)
@Getter
public class DropSubTableData extends GameResource {
    @Getter(onMethod = @__(@Override))
    private int id;
    private int randomType;
    private int dropLevel;
    private List<DropItemData> dropVec;
    private int nodeType;
}
