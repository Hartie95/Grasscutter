package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.friendship.AvatarFetterLevelRewardRsp;

public class PacketAvatarFetterLevelRewardRsp extends BaseTypedPacket<AvatarFetterLevelRewardRsp> {
	public PacketAvatarFetterLevelRewardRsp(long guid, int fetterLevel, int rewardId) {
        super(new AvatarFetterLevelRewardRsp());
        proto.setAvatarGuid(guid);
        proto.setFetterLevel(fetterLevel);
        proto.setRetcode(0);
        proto.setRewardId(rewardId);
	}

    public PacketAvatarFetterLevelRewardRsp(long guid, int fetterLevel) {
        super(new AvatarFetterLevelRewardRsp());
        proto.setAvatarGuid(guid);
        proto.setFetterLevel(fetterLevel);
        proto.setRetcode(1);
        proto.setRewardId(0);
	}
}
