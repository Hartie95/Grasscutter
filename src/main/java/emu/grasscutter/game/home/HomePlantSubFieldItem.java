package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.HomeBlockSubFieldDataOuterClass.HomeBlockSubFieldData;
import emu.grasscutter.net.proto.HomePlantFieldStatusOuterClass.HomePlantFieldStatus;
import emu.grasscutter.net.proto.HomePlantSubFieldDataOuterClass.HomePlantSubFieldData;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class HomePlantSubFieldItem {
    List<Integer> entityIdList;
    int fieldStatus; // homep plant field status in number
    int homeGatherId;
    int seedId;
    int endTime; 

    public HomePlantSubFieldData toProto() {
        return HomePlantSubFieldData.newBuilder()
            .addAllEntityIdList(entityIdList)
            .setFieldStatus(HomePlantFieldStatus.valueOf(fieldStatus))
            .setHomeGatherId(homeGatherId)
            .setSeedId(seedId)
            .setEndTime(endTime)
            .build();
    }

    public static HomePlantSubFieldItem parseFrom(HomeBlockSubFieldData subFieldData) {
        return HomePlantSubFieldItem.of()
            .entityIdList(List.of())
            .fieldStatus(HomePlantFieldStatus.HOME_PLANT_FIELD_STATUS_STATUE_NONE.getNumber())
            .homeGatherId(0)
            .seedId(0)
            .endTime(0)
            .build();
    }

    public void setProperties(List<Integer> entityIdList, HomePlantFieldStatus fieldStatus, int homeGatherId, int seedId, int endTime) {
        this.entityIdList = entityIdList;
        this.fieldStatus = fieldStatus.getNumber();
        this.homeGatherId = homeGatherId;
        this.seedId = seedId;
        this.endTime = endTime;
    }

    public boolean isEmpty() {
        return fieldStatus == HomePlantFieldStatus.HOME_PLANT_FIELD_STATUS_STATUE_NONE.getNumber();
    }

    public void reset() {
        this.entityIdList = List.of();
        this.fieldStatus = HomePlantFieldStatus.HOME_PLANT_FIELD_STATUS_STATUE_NONE.getNumber();
        this.homeGatherId = 0;
        this.seedId = 0;
        this.endTime = 0;
    }
}
