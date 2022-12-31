package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@ResourceType(name = "MapAreaConfigData.json")
@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class MapAreaData extends GameResource {
    int id;
    int sceneID;
    // name // just comments
    // "OCMPIJHGFDD": [210], // not sure what this is for
    @SerializedName(value="state", alternate={"FLFGPJKIKFM"})
    String state; // guessing the real name, might be wrong

    @Override
    public int getId() {
        return this.id;
    }
}
