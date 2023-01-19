package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.talk.TalkExec;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@ResourceType(name = "TalkExcelConfigData.json")
@EqualsAndHashCode(callSuper=false)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TalkConfigData extends GameResource {
    @SerializedName(value="id", alternate={"_id"})
    int id;
    @SerializedName(value="finishExec", alternate={"_finishExec"})
    List<TalkExecParam> finishExec;
    @SerializedName(value="questId", alternate={"_questId"})
    int questId;

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void onLoad() {
        this.finishExec = this.finishExec == null ? List.of() : 
            this.finishExec.stream().filter(x -> x.getType() != null).toList();
    }

    @Data
    public static class TalkExecParam {
        @SerializedName(value="type", alternate={"_type"})
        TalkExec type;
        @SerializedName(value="param", alternate={"_param"})
        String[] param;
    }
}
