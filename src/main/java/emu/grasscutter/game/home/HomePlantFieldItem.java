package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.HomeWorldPlantData;
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
public class HomePlantFieldItem {
    List<HomePlantSubFieldItem> subFieldList;
    int furnitureId;
    int sceneId;
    int fieldGuid;
    Position spawnPos;

    public HomePlantFieldData toProto() {
        HomePlantFieldData.Builder proto = HomePlantFieldData.newBuilder();
        subFieldList.stream().forEach(subFieldItem -> proto.addSubFieldList(subFieldItem.toProto()));
        return proto.setFurnitureId(furnitureId)
            .setSceneId(sceneId)
            .setFieldGuid(fieldGuid)
            .setSpawnPos(spawnPos.toProto())             
            .build();
    }

    public static HomePlantFieldItem parseFrom(int sceneId, HomeBlockFieldData fieldData) {
        return HomePlantFieldItem.of()
            .subFieldList(fieldData.getSubFieldListList().stream()
                .map(HomePlantSubFieldItem::parseFrom)
                .toList())
            .furnitureId(fieldData.getFurnitureId())
            .sceneId(sceneId)
            .fieldGuid(fieldData.getGuid())
            .spawnPos(new Position(fieldData.getPos()))
            .build();
    }
    
    public HomePlantSubFieldItem getSubFieldByIndex(int index) {
        return getSubFieldList().get(index);
    }

    public HomePlantSubFieldItem getNextEmptySubField() {
        return subFieldList.stream()
            .filter(subField -> subField.isEmpty())
            .findFirst()
            .orElse(null);
    }

    public boolean subFieldIsEmpty(int index) {
        return getSubFieldByIndex(index).isEmpty();
    }

    public boolean setSubFieldPropertiesByIndex(int index, int seedId) {
        if (index < 0 || getSubFieldList().size() < index+1
            || !subFieldIsEmpty(index)) return false;

        return setSubFieldProperties(getSubFieldByIndex(index), seedId);
    }

    public boolean setSubFieldProperties(HomePlantSubFieldItem subFieldItem, int seedId) {
        return setSubFieldProperties(subFieldItem, seedId, List.of(), HomePlantFieldStatus.HOME_PLANT_FIELD_STATUS_STATUE_SEED);
    }

    public boolean setSubFieldProperties(HomePlantSubFieldItem subFieldItem, int seedId, List<Integer> entityIdList, HomePlantFieldStatus status) {
        if (subFieldItem == null) return false;

        HomeWorldPlantData plantData = GameData.getHomeWorldPlantDataMap().get(seedId);
        if (plantData == null) return false;

        subFieldItem.setProperties(
            entityIdList,
            status, 
            plantData.getHomeGatherId(), 
            seedId, 
            (int) (System.currentTimeMillis() / 1000) + plantData.getTime());
        return true;
    }

    public boolean setAllSubFieldProperties(int index, List<Integer> seedId) {
        if (seedId.size() <= 1) return setSubFieldPropertiesByIndex(index, seedId.get(0));

        for (int seed : seedId) {            
            if (!setSubFieldProperties(getNextEmptySubField(), seed)) return false;
        }

        return true; // return true if all seeds are planted
    }
}
