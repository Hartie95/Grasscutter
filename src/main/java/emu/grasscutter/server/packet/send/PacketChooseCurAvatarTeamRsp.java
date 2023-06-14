package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ChooseCurAvatarTeamRspOuterClass.ChooseCurAvatarTeamRsp;

public class PacketChooseCurAvatarTeamRsp extends BasePacket {

	public PacketChooseCurAvatarTeamRsp(int retVal, int teamId) {
		super(PacketOpcodes.ChooseCurAvatarTeamRsp);

		ChooseCurAvatarTeamRsp proto = ChooseCurAvatarTeamRsp.newBuilder()
            .setCurTeamId(teamId)
            .setRetcode(retVal)
            .build();

		this.setData(proto);
	}
}
