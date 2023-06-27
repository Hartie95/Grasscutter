package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.managers.blossom.BlossomManager;
import emu.grasscutter.game.world.World;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.WorldOwnerBlossomBriefInfoNotifyOuterClass.WorldOwnerBlossomBriefInfoNotify;

import java.util.List;

public class PacketWorldOwnerBlossomBriefInfoNotify extends BasePacket {
    public PacketWorldOwnerBlossomBriefInfoNotify(World world) {
        super(PacketOpcodes.WorldOwnerBlossomBriefInfoNotify);

        BlossomManager blossomManager = world.getOwner().getBlossomManager();
        this.setData(WorldOwnerBlossomBriefInfoNotify.newBuilder()
                .addAllBriefInfoList(blossomManager == null ? List.of() : blossomManager.getBriefInfo())
            .build());
    }
}
