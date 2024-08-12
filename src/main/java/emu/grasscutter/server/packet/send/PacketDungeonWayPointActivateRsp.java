package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.progression.DungeonWayPointActivateRsp;

public class PacketDungeonWayPointActivateRsp extends BaseTypedPacket<DungeonWayPointActivateRsp> {
	public PacketDungeonWayPointActivateRsp(boolean success, int pointId) {
        super(new DungeonWayPointActivateRsp());
        proto.setWayPointId(pointId);
        proto.setRetcode(success ? RetcodeOuterClass.Retcode.RET_SUCC_VALUE : RetcodeOuterClass.Retcode.RET_FAIL_VALUE);
	}
}
