package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeResourceTakeHomeCoinRspOuterClass.HomeResourceTakeHomeCoinRsp;

public class PacketHomeResourceTakeHomeCoinRsp extends BasePacket {

    public PacketHomeResourceTakeHomeCoinRsp(Player player) {
        super(PacketOpcodes.HomeResourceTakeHomeCoinRsp);
        // client shouldnt trigger packet request if coin is 0, just to make sure
        if (!player.getHome().receiveHomeCoin()) return;

        HomeResourceTakeHomeCoinRsp.Builder proto = HomeResourceTakeHomeCoinRsp.newBuilder();
        player.sendPacket(new PacketHomeResourceNotify(player));
        proto.setHomeCoin(player.getHome().homeCoinResourceToProto()).build();
        this.setData(proto);
    }
}
