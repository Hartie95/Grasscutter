package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomePlantSeedRspOuterClass.HomePlantSeedRsp;


public class PacketHomePlantSeedRsp extends BasePacket {

    public PacketHomePlantSeedRsp(Player player, Retcode result) {
        super(PacketOpcodes.HomePlantSeedRsp);

        this.setData(HomePlantSeedRsp.newBuilder()
            .setRetcode(result.getNumber())
            .build());
    }
}
