package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonRestartRspOuterClass;

public class PacketDungeonRestartRsp extends BasePacket {
    public PacketDungeonRestartRsp() {
        super(PacketOpcodes.DungeonRestartRsp);
        this.setData(DungeonRestartRspOuterClass.DungeonRestartRsp.newBuilder());
    }
}
