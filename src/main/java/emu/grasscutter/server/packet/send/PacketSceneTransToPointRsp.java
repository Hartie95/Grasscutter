package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.net.proto.SceneTransToPointRspOuterClass.SceneTransToPointRsp;

public class PacketSceneTransToPointRsp extends BasePacket {

	public PacketSceneTransToPointRsp(boolean result, int pointId, int sceneId) {
		super(PacketOpcodes.SceneTransToPointRsp);

		this.setData(SceneTransToPointRsp.newBuilder()
            .setRetcode(result ? Retcode.RET_SUCC_VALUE : Retcode.RET_SVR_ERROR_VALUE) // Internal server error
            .setPointId(pointId)
            .setSceneId(sceneId)
            .build());
	}
}
