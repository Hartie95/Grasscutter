package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = {"ViewCodexExcelConfigData.json"})
public class CodexViewpointData extends GameResource {
    @SerializedName(value = "id", alternate = "Id")
    public int id;
    public int gadgetId;
    public int sceneId;
    public int groupId;
    public int configId;
    private long nameTextMapHash;
    private long descTextMapHash;
    private String image;
    private int cityId;
    private int worldAreaId;
    private int sortOrder;


    @Override
    public void onLoad() {
        GameData.getCodexViewpointDataIdMap().put(this.getGroupId()<<32 + getConfigId(), this);
    }
}
