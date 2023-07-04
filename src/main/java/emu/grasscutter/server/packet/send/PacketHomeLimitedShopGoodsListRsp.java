package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeLimitedShopGoodsListRspOuterClass.HomeLimitedShopGoodsListRsp;

public class PacketHomeLimitedShopGoodsListRsp extends BasePacket {

    public PacketHomeLimitedShopGoodsListRsp(Player player) {
        super(PacketOpcodes.HomeLimitedShopGoodsListRsp);
        // the fact that this is triggered, teapot is initialised.
        this.setData(HomeLimitedShopGoodsListRsp.newBuilder()
            .setShop(player.getHome().getWeeklyDjin().shopToProto(player))
            .build());
    }
}
