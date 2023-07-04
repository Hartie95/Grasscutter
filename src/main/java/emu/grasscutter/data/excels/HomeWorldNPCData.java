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
@ResourceType(name = {"HomeWorldNPCExcelConfigData.json"})
public class HomeWorldNPCData extends GameResource {
    private int furnitureID;
    private int avatarID;
    @SerializedName(value="isPaimon", alternate={"JJNCEPKMJFL"})
    private boolean isPaimon; // im guessing the real name, could be wrong
    // properties not used but might be useful
    // "MFMNMLFOFCI": 9900,
    // might be talk data
    // "LKKEEMCLIJI": [],
    // these are properties for paimon
    // "BDBDDNNGNHH": "UI_AvatarIcon_Side_Paimon",
    // "ACLHHPHMHII": "UI_AvatarIcon_Paimon",
    // "HCPCABALDGH": "UI_AvatarIcon_Side_Paimon",
    // "ODCDGACOBCN": "QUALITY_ORANGE",
    // "FINDHIIALPA": 93920314,
    // "descTextMapHash": 2038432562

    @Override
    public int getId() {
        return this.furnitureID;
    }
}
