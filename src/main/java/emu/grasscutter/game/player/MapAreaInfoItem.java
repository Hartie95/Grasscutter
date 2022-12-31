package emu.grasscutter.game.player;

import dev.morphia.annotations.Entity;
import emu.grasscutter.net.proto.MapAreaInfoOuterClass.MapAreaInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity @Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class MapAreaInfoItem {
    int mapAreaId;
    boolean isOpen;

    public static MapAreaInfoItem create(int id, boolean isOpen) {
        return MapAreaInfoItem.of()
            .mapAreaId(id)
            .isOpen(isOpen)
            .build();
    }

    public void changeState(int state) {
        this.isOpen = state == 1;
    }

    public MapAreaInfo toProto() {
        return MapAreaInfo.newBuilder()
            .setMapAreaId(getMapAreaId())
            .setIsOpen(isOpen())
            .build();
    }
}