package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.BlossomScheduleInfoOuterClass.BlossomScheduleInfo;
import emu.grasscutter.net.proto.WorldOwnerBlossomScheduleInfoNotifyOuterClass.WorldOwnerBlossomScheduleInfoNotify;

import java.util.List;

public class PacketWorldOwnerBlossomScheduleInfoNotify extends BasePacket {
    public PacketWorldOwnerBlossomScheduleInfoNotify(BlossomScheduleInfo scheduleInfo) {
        super(PacketOpcodes.WorldOwnerBlossomScheduleInfoNotify);

        this.setData(WorldOwnerBlossomScheduleInfoNotify.newBuilder()
            .setScheduleInfo(scheduleInfo)
            .build());
    }
}
