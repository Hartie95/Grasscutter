package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonReviseLevelNotifyOuterClass.DungeonReviseLevelNotify;

public class PacketDungeonReviseLevelNotify extends BasePacket {
    public PacketDungeonReviseLevelNotify(DungeonData dungeonData) {
        super(PacketOpcodes.DungeonReviseLevelNotify);
        this.setData(DungeonReviseLevelNotify.newBuilder()
            .setDungeonId(dungeonData.getId())
            .setReviseLevel(dungeonData.getLevelRevise())
            .setShowLevel(dungeonData.getShowLevel()));
    }
}
