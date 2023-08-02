package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonDataNotifyOuterClass;

public class PacketDungeonDataNotify extends BasePacket {
    public PacketDungeonDataNotify(DungeonData dungeonData) {
        super(PacketOpcodes.DungeonDataNotify);
        this.setData(DungeonDataNotifyOuterClass.DungeonDataNotify.newBuilder());
    }
}
