package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.general.ServerTimeNotify;

public class PacketServerTimeNotify extends BaseTypedPacket<ServerTimeNotify> {

	public PacketServerTimeNotify() {
        super(new ServerTimeNotify());
        proto.setServerTime(System.currentTimeMillis());
	}
}
