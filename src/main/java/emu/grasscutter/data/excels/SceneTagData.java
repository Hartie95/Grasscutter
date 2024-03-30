package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Getter;
import java.util.List;

@ResourceType(name = "SceneTagConfigData.json")
@Getter
public class SceneTagData extends GameResource {
	@Getter(onMethod = @__(@Override))
	private int id;
	private String sceneTagName;
	private int sceneId;
	private boolean isDefaultValid;
	private boolean isSkipLoading;
}
