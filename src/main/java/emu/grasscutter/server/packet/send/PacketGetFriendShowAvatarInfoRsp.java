package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.community.friends.GetFriendShowAvatarInfoRsp;
import org.anime_game_servers.multi_proto.gi.messages.general.avatar.ShowAvatarInfo;

import java.util.List;

public class PacketGetFriendShowAvatarInfoRsp extends BaseTypedPacket<GetFriendShowAvatarInfoRsp> {

	public PacketGetFriendShowAvatarInfoRsp(int uid, List<ShowAvatarInfo> showAvatarInfoList) {
        super(new GetFriendShowAvatarInfoRsp());
        proto.setUid(uid);
        proto.setShowAvatarInfoList(showAvatarInfoList);
	}

}
