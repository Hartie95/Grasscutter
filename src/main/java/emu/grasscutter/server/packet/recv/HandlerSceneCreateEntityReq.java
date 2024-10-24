package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.TypedPacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketSceneCreateEntityRsp;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneCreateEntityReq;

import static emu.grasscutter.game.world.SceneHelpers.createEntityFromCreateData;

public class HandlerSceneCreateEntityReq extends TypedPacketHandler<SceneCreateEntityReq> {

    @Override
    public void handle(GameSession session, byte[] header, SceneCreateEntityReq req) throws Exception {
        val entity = req.getEntity();

        if(entity == null || entity.getEntity() == null){
            Grasscutter.getLogger().warn("SceneCreateEntityReq.entity null, either an invalid packet, or the parsing failed");
            return;
        }

        // TODO handle extra fields
        val createdEntityId = createEntityFromCreateData(session.getPlayer(), entity);

        session.send(new PacketSceneCreateEntityRsp(createdEntityId, req));
    }
}
