package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.BlossomBriefInfoNotifyOuterClass.BlossomBriefInfoNotify;
import emu.grasscutter.net.proto.BlossomBriefInfoOuterClass.BlossomBriefInfo;

public class PacketBlossomBriefInfoNotify extends BasePacket {
    public PacketBlossomBriefInfoNotify(Iterable<BlossomBriefInfo> blossoms) {
        super(PacketOpcodes.BlossomBriefInfoNotify);
        this.setData(BlossomBriefInfoNotify.newBuilder()
            .addAllBriefInfoList(blossoms));
    }
}
