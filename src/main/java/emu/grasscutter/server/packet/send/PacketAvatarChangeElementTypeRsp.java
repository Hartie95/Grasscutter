package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.AvatarChangeElementTypeRsp;

public class PacketAvatarChangeElementTypeRsp extends BaseTypedPacket<AvatarChangeElementTypeRsp> {

	public PacketAvatarChangeElementTypeRsp() {
        super(new AvatarChangeElementTypeRsp());
	}

	public PacketAvatarChangeElementTypeRsp(int retcode) {
        super(new AvatarChangeElementTypeRsp());
        proto.setRetcode(retcode);
	}
}
