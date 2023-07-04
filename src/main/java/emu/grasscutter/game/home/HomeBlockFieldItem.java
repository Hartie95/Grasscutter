package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.HomeBlockFieldDataOuterClass.HomeBlockFieldData;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class HomeBlockFieldItem {
    Position pos;
    Position rot;
    int guid;
    int furnitureId;
    List<HomeBlockSubFieldItem> subFieldList;
    
    public HomeBlockFieldData toProto() {
        return HomeBlockFieldData.newBuilder()
            .setPos(pos.toProto())
            .setRot(rot.toProto())
            .setGuid(guid)
            .setFurnitureId(furnitureId)
            .addAllSubFieldList(subFieldList.stream()
                .map(HomeBlockSubFieldItem::toProto)
                .toList())
            .build();
    }
    public static HomeBlockFieldItem parseFrom(HomeBlockFieldData fieldData) {
        return HomeBlockFieldItem.of()
            .pos(new Position(fieldData.getPos()))
            .rot(new Position(fieldData.getRot()))
            .guid(fieldData.getGuid())
            .furnitureId(fieldData.getFurnitureId())
            .subFieldList(fieldData.getSubFieldListList().stream()
                .map(HomeBlockSubFieldItem::parseFrom)
                .toList())
            .build();
    }

    
}
