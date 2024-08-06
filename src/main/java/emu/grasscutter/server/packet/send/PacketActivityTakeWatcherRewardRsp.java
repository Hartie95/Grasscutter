package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.activity.ActivityTakeWatcherRewardRsp;

public class PacketActivityTakeWatcherRewardRsp extends BaseTypedPacket<ActivityTakeWatcherRewardRsp> {

	public PacketActivityTakeWatcherRewardRsp(int activityId, int watcherId) {
        super(new ActivityTakeWatcherRewardRsp());
        proto.setActivityId(activityId);
        proto.setWatcherId(watcherId);
	}

}
