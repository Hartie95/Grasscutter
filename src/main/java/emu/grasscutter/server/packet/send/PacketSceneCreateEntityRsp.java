package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.general.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneCreateEntityReq;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneCreateEntityRsp;

public class PacketSceneCreateEntityRsp extends BaseTypedPacket<SceneCreateEntityRsp> {

	public PacketSceneCreateEntityRsp(int entityId, SceneCreateEntityReq req) {
		super(new SceneCreateEntityRsp());
        proto.setEntityId(entityId);
        proto.setEntity(req.getEntity());

        if(entityId < 0) {
            proto.setRetCode(Retcode.RET_FAIL);
        }
	}
}
