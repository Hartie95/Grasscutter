package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.PlayerEnterDungeonRsp;

public class PacketPlayerEnterDungeonRsp extends BaseTypedPacket<PlayerEnterDungeonRsp> {

	public PacketPlayerEnterDungeonRsp(int pointId, int dungeonId, boolean success) {
        super(new PlayerEnterDungeonRsp());
        proto.setPointId(pointId);
        proto.setDungeonId(dungeonId);
        proto.setRetcode(success ? RetcodeOuterClass.Retcode.RET_SUCC_VALUE : RetcodeOuterClass.Retcode.RET_FAIL_VALUE);

	}
}
