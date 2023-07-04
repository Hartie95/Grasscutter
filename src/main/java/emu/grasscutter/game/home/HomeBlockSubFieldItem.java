package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.HomeBlockSubFieldDataOuterClass.HomeBlockSubFieldData;
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
public class HomeBlockSubFieldItem {
    Position pos;
    Position rot;

    public HomeBlockSubFieldData toProto() {
        return HomeBlockSubFieldData.newBuilder()
            .setPos(pos.toProto())
            .setRot(pos.toProto())
            .build();
    }

    public static HomeBlockSubFieldItem parseFrom(HomeBlockSubFieldData subFieldData) {
        return HomeBlockSubFieldItem.of()
            .pos(new Position(subFieldData.getPos()))
            .rot(new Position(subFieldData.getRot()))
            .build();
    }
}
