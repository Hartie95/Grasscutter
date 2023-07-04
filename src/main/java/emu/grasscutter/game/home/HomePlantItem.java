package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.HomeBlockFieldDataOuterClass.HomeBlockFieldData;
import emu.grasscutter.net.proto.HomeBlockSubFieldDataOuterClass.HomeBlockSubFieldData;
import emu.grasscutter.net.proto.HomePlantFieldDataOuterClass.HomePlantFieldData;
import emu.grasscutter.net.proto.HomePlantFieldStatusOuterClass.HomePlantFieldStatus;
import emu.grasscutter.net.proto.HomePlantSubFieldDataOuterClass.HomePlantSubFieldData;
import emu.grasscutter.net.proto.VectorOuterClass.Vector;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class HomePlantItem {
    List<HomePlantFieldItem> fieldList;

    public static HomePlantItem parseFrom(int sceneId, List<HomeBlockFieldData> fieldDataList) {
        return HomePlantItem.of()
            .fieldList(fieldDataList.stream()
                .map(blockField -> HomePlantFieldItem.parseFrom(sceneId, blockField))
                .toList())
            .build();
    }

    public HomePlantFieldItem getFieldByGuid(int guid) {
        return fieldList.stream()
            .filter(fieldItem -> fieldItem.getFieldGuid() == guid)
            .findAny()
            .orElse(null);
    }

    public List<Integer> getFieldsGuid() {
        return fieldList.stream()
            .map(HomePlantFieldItem::getFieldGuid)
            .toList();
    }

    public void update(List<HomePlantFieldItem> newPlantFieldItem) {
        this.fieldList = newPlantFieldItem.stream().map(fieldItem -> {
            if (getFieldsGuid().contains(fieldItem.getFieldGuid())) {
                return getFieldByGuid(fieldItem.getFieldGuid());
            }
            return fieldItem;
        }).toList();
    }
}
