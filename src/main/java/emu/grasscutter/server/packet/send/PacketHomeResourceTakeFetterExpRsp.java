package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeResourceTakeFetterExpRspOuterClass.HomeResourceTakeFetterExpRsp;

public class PacketHomeResourceTakeFetterExpRsp extends BasePacket {

    public PacketHomeResourceTakeFetterExpRsp(Player player) {
        super(PacketOpcodes.HomeResourceTakeFetterExpRsp);
        
        player.getHome().receiveFetterExp();
        HomeResourceTakeFetterExpRsp.Builder proto = HomeResourceTakeFetterExpRsp.newBuilder();
        player.sendPacket(new PacketHomeResourceNotify(player));
        proto.setFetterExp(player.getHome().fetterExpResourceToProto()).build();
        this.setData(proto);
    }
}
