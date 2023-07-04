package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.home.HomeSceneItem;
import emu.grasscutter.game.home.HomeFurnitureItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.HomeTransferRspOuterClass.HomeTransferRsp;

import java.util.List;

public class PacketHomeTransferRsp extends BasePacket {

    public PacketHomeTransferRsp(Player player, int guid) {
        super(PacketOpcodes.HomeTransferRsp);
        HomeTransferRsp.Builder proto = HomeTransferRsp.newBuilder();

        HomeSceneItem homeScene = player.getHome().getHomeSceneItem();
        List<HomeFurnitureItem> furniture = homeScene.getBlockItems().values().stream()
            .map(x -> x.getDeployFurnitureByGuid(guid))
            .toList();

        HomeFurnitureItem teleportPoint = furniture.stream()
            .filter(x -> x != null)
            .findFirst()
            .orElse(null);
        
        if (teleportPoint != null) {
            // not sure what the offset for teleport pos is
            player.getWorld().transferPlayerToScene(player, homeScene.getSceneId(), teleportPoint.getSpawnPos().clone().addZ(-2));
        }

        this.setData(proto.build());
    }
}
