package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.BlossomChestCreateNotifyOuterClass.BlossomChestCreateNotify;

public class PacketBlossomChestCreateNotify extends BasePacket {
    public PacketBlossomChestCreateNotify(int refreshId, int campId) {
        super(PacketOpcodes.BlossomChestCreateNotify);
        this.setData(BlossomChestCreateNotify.newBuilder()
            .setRefreshId(refreshId)
            .setCircleCampId(campId));
    }
}
