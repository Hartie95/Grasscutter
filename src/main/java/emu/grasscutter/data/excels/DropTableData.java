package emu.grasscutter.data.excels;

import emu.grasscutter.data.ResourceType;
import emu.grasscutter.data.ResourceType.LoadPriority;
import lombok.Getter;

@ResourceType(name = "DropTableExcelConfigData.json", loadPriority = LoadPriority.HIGH)
@Getter
public class DropTableData extends DropSubTableData {
    private boolean fallToGround;
    private int sourceType;
    private int everydayLimit;
    private int historyLimit;
    private int activityLimit;
}
