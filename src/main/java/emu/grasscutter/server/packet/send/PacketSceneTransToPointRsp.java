package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneTransToPointRsp;

public class PacketSceneTransToPointRsp extends BaseTypedPacket<SceneTransToPointRsp> {
    public PacketSceneTransToPointRsp(boolean result, int pointId, int sceneId) {
        super(new SceneTransToPointRsp());
        proto.setPointId(pointId);
        proto.setRetcode(result ? Retcode.RET_SUCC_VALUE : Retcode.RET_SVR_ERROR_VALUE);
        proto.setSceneId(sceneId);
    }
}
