package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.PlayerEnterDungeonRspOuterClass.PlayerEnterDungeonRsp;
import emu.grasscutter.net.proto.RetcodeOuterClass;

public class PacketPlayerEnterDungeonRsp extends BasePacket {

	public PacketPlayerEnterDungeonRsp(int pointId, int dungeonId, boolean success) {
		super(PacketOpcodes.PlayerEnterDungeonRsp);

		PlayerEnterDungeonRsp proto = PlayerEnterDungeonRsp.newBuilder()
				.setPointId(pointId)
				.setDungeonId(dungeonId)
            .setRetcode(success ? RetcodeOuterClass.Retcode.RET_SUCC_VALUE : RetcodeOuterClass.Retcode.RET_FAIL_VALUE)
				.build();

		this.setData(proto);
	}
}
