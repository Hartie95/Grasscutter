package emu.grasscutter.data.excels;

import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@ResourceType(name = "SceneTagConfigData.json")
@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class SceneTagData extends GameResource {
    int id;
    String sceneTagName;
    int sceneId;
    boolean isDefaultValid;
    // not sure what these two are
    // "OINJFCJEPNF": true,
    // "MPCPJKAPGLF": true,
    SceneTagCondType[] cond;

    @Override
    public int getId() {
        return this.id;
    }

    @Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
    public class SceneTagCondType {
        String condType;
        int param1;
        int param2;
    }
}
