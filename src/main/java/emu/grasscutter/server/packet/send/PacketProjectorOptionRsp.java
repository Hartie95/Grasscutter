package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.gadget.ProjectorOptionRsp;

public class PacketProjectorOptionRsp extends BaseTypedPacket<ProjectorOptionRsp> {

    public PacketProjectorOptionRsp(int entityId, int opType, int retcode) {
        super(new ProjectorOptionRsp());
        proto.setOpType(opType);
        proto.setEntityId(entityId);
        proto.setRetCode(retcode);
    }
}
