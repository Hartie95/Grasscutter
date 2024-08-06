package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.activity.aster.AsterMiscInfoNotify;

public class PacketAsterMiscInfoNotify extends BaseTypedPacket<AsterMiscInfoNotify> {

	public PacketAsterMiscInfoNotify(int credit, int token) {
        super(new AsterMiscInfoNotify());
        proto.setAsterCredit(credit);
        proto.setAsterToken(token);
	}
}
