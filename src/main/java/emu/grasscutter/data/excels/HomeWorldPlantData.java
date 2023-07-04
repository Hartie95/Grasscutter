package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper=false)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ResourceType(name = {"HomeWorldPlantExcelConfigData.json"})
public class HomeWorldPlantData extends GameResource {
    @SerializedName(value="seedId", alternate={"NAAPOJEHBLM"})
    private int seedId;
    @SerializedName(value="homeGatherId", alternate={"PPLOLMMAIFM"})
    private int homeGatherId;
    @SerializedName(value="furnitureId", alternate={"KDLGHPEHOCE"})
    private int furnitureId;
    private int time;
    private int order;
    private int dropID;
    @SerializedName(value="gadgetId", alternate={"BKBNGLBHABM"})
    private int gadgetId;
    // properties not used but might be useful
    // "BMMMAHBEOLP": [
    //     2004,
    //     2058
    // ],
    // "HAJPOHGPACD": [
    //     {
    //         "PPLOLMMAIFM": 7002,
    //         "dropID": 21310002,
    //         "weight": 20
    //     },
    //     {},
    //     {}
    // ],
    // "FBPFDFOELOA": 86400,
    // "PHFFPLKMGMI": 1,
    // "inteeIconName": "",
    // "BLNKACNJCAD": 145620177

    @Override
    public int getId() {
        return this.seedId;
    }
}
