package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.SetUpAvatarTeamRspOuterClass.SetUpAvatarTeamRsp;

import java.util.Collection;

public class PacketSetUpAvatarTeamRsp extends BasePacket {
    public PacketSetUpAvatarTeamRsp(int teamId, long guid, Collection<Long> guidList, int retVal) {
        super(PacketOpcodes.SetUpAvatarTeamRsp);

        SetUpAvatarTeamRsp.Builder proto = SetUpAvatarTeamRsp.newBuilder()
            .setTeamId(teamId)
            .setCurAvatarGuid(guid)
            .addAllAvatarTeamGuidList(guidList)
            .setRetcode(retVal);

        this.setData(proto);
    }
}
