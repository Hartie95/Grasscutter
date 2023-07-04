package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.HomeworldDefaultSaveData;
import emu.grasscutter.data.excels.HomeWorldNPCData;
import emu.grasscutter.data.excels.ItemData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.HomeFurnitureDataOuterClass.HomeFurnitureData;
import emu.grasscutter.net.proto.HomeMarkPointFurnitureDataOuterClass.HomeMarkPointFurnitureData;
import emu.grasscutter.net.proto.HomeMarkPointNPCDataOuterClass.HomeMarkPointNPCData;
import emu.grasscutter.net.proto.VectorOuterClass;
import emu.grasscutter.utils.Position;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class HomeFurnitureItem {
    int furnitureId;
    int guid;
    int parentFurnitureIndex;
    Position spawnPos;
    Position spawnRot;
    int version;

    public HomeFurnitureData toProto(){
        return HomeFurnitureData.newBuilder()
                .setFurnitureId(furnitureId)
                .setGuid(guid)
                .setParentFurnitureIndex(parentFurnitureIndex)
                .setSpawnPos(spawnPos.toProto())
                .setSpawnRot(spawnRot.toProto())
                .setVersion(version)
                .build();
    }

    public HomeMarkPointFurnitureData toMarkPointProto(Player player){
        HomeMarkPointFurnitureData.Builder proto = HomeMarkPointFurnitureData.newBuilder();
        proto.setFurnitureId(furnitureId)
            .setGuid(guid)
            .setPos(spawnPos.toProto());

        if (getAsItem() != null && getAsItem().getMarkPointType() != null) {
            proto.setFurnitureType(
                switch (getAsItem().getMarkPointType()) {
                    // TODO add more types
                    case "Apartment" -> 6;
                    case "FarmField" -> 2;
                    case "NPC" -> 5;
                    case "Paimon" -> 8;
                    case "TeleportPoint" -> 3;
                    default -> 0;
                }
            );
        }
        
        if (isNPC() && !isPaimon()) {
            proto.setNpcData(HomeMarkPointNPCData.newBuilder()
                .setAvatarId(getAvatarId())
                .setCostumeId(player.getAvatars().getAvatarById(getAvatarId()).getCostume())
                .build());
        }
            
        return proto.build();
    }

    public static HomeFurnitureItem parseFrom(HomeFurnitureData homeFurnitureData) {
        return HomeFurnitureItem.of()
                .furnitureId(homeFurnitureData.getFurnitureId())
                .guid(homeFurnitureData.getGuid())
                .parentFurnitureIndex(homeFurnitureData.getParentFurnitureIndex())
                .spawnPos(new Position(homeFurnitureData.getSpawnPos()))
                .spawnRot(new Position(homeFurnitureData.getSpawnRot()))
                .version(homeFurnitureData.getVersion())
                .build();
    }

    public static HomeFurnitureItem parseFrom(HomeworldDefaultSaveData.HomeFurniture homeFurniture) {
        return HomeFurnitureItem.of()
                .furnitureId(homeFurniture.getId())
                .parentFurnitureIndex(1)
                .spawnPos(homeFurniture.getPos() == null ? new Position() : homeFurniture.getPos())
                .spawnRot(homeFurniture.getRot() == null ? new Position() : homeFurniture.getRot())
                .build();
    }

    public ItemData getAsItem() {
        return GameData.getItemDataMap().get(this.furnitureId);
    }

    public int getComfort() {
        var item = getAsItem();

        if (item == null){
            return 0;
        }
        return item.getComfort();
    }

    public HomeWorldNPCData getNPCData() {
        return GameData.getHomeWorldNPCDataMap().get(this.furnitureId);
    }

    public int getAvatarId() {
        return getNPCData().getAvatarID();
    }

    public boolean isNPC() {
        return getNPCData() != null;
    }

    public boolean isPaimon() {
        return getNPCData().isPaimon();
    }
}
