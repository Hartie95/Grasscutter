package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.team.ChooseCurAvatarTeamRsp;

public class PacketChooseCurAvatarTeamRsp extends BaseTypedPacket<ChooseCurAvatarTeamRsp> {
	public PacketChooseCurAvatarTeamRsp(int retVal, int teamId) {
        super(new ChooseCurAvatarTeamRsp());
        proto.setCurTeamId(teamId);
        proto.setRetcode(retVal);
	}
}
