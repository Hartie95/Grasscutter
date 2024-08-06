package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.DungeonSlipRevivePointActivateRsp;

public class PacketDungeonSlipRevivePointActivateRsp extends BaseTypedPacket<DungeonSlipRevivePointActivateRsp> {
	public PacketDungeonSlipRevivePointActivateRsp(boolean success, int pointId) {
        super(new DungeonSlipRevivePointActivateRsp());
        proto.setSlipRevivePointId(pointId);
        proto.setRetcode(success ? RetcodeOuterClass.Retcode.RET_SUCC_VALUE : RetcodeOuterClass.Retcode.RET_FAIL_VALUE);
	}
}
