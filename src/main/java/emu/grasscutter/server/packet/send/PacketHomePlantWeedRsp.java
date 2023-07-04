package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomePlantWeedRspOuterClass.HomePlantWeedRsp;

public class PacketHomePlantWeedRsp extends BasePacket {

    public PacketHomePlantWeedRsp(Retcode result) {
        super(PacketOpcodes.HomePlantWeedRsp);
        this.setData(HomePlantWeedRsp.newBuilder().setRetcode(result.getNumber()).build());
    }
}
