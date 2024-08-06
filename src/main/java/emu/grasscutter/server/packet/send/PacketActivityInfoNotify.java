package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.activity.ActivityInfo;
import org.anime_game_servers.multi_proto.gi.messages.activity.ActivityInfoNotify;

public class PacketActivityInfoNotify extends BaseTypedPacket<ActivityInfoNotify> {

	public PacketActivityInfoNotify(ActivityInfo activityInfo) {
        super(new ActivityInfoNotify());
        proto.setActivityInfo(activityInfo);
	}
}
