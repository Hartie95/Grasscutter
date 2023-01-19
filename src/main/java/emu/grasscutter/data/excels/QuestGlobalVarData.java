package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;;

@ResourceType(name = "QuestGlobalVarConfigData.json")
@EqualsAndHashCode(callSuper=false)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestGlobalVarData extends GameResource {
    int id;
    int defaultValue;

    @Override
    public int getId() {
        return this.id;
    }
}
