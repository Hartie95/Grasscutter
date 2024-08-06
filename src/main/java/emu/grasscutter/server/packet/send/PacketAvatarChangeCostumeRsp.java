package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.cosmetic.AvatarChangeCostumeRsp;

public class PacketAvatarChangeCostumeRsp extends BaseTypedPacket<AvatarChangeCostumeRsp> {

	public PacketAvatarChangeCostumeRsp(long avatarGuid, int costumeId) {
        super(new AvatarChangeCostumeRsp());
        proto.setCostumeId(costumeId);
        proto.setAvatarGuid(avatarGuid);
	}

	public PacketAvatarChangeCostumeRsp() {
		super(new AvatarChangeCostumeRsp());
        proto.setRetcode(RetcodeOuterClass.Retcode.RET_SVR_ERROR_VALUE);
	}
}
