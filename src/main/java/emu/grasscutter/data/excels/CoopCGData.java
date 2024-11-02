package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Getter;

@ResourceType(name = "CoopCGExcelConfigData.json")
@Getter
public class CoopCGData extends GameResource {
	@Getter(onMethod = @__(@Override))
	private int id;
	private int chapterId;
}
