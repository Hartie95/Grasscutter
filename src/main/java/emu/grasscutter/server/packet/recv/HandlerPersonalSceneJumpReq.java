package emu.grasscutter.server.packet.recv;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScenePointEntry;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.PersonalSceneJumpReqOuterClass.PersonalSceneJumpReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketPersonalSceneJumpRsp;
import lombok.val;

import java.util.Optional;


@Opcodes(PacketOpcodes.PersonalSceneJumpReq)
public class HandlerPersonalSceneJumpReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        val req = PersonalSceneJumpReq.parseFrom(payload);
        val player = session.getPlayer();

        // get the scene point
        val pointData = Optional.ofNullable(GameData.getScenePointEntryById(player.getSceneId(), req.getPointId()))
            .map(ScenePointEntry::getPointData).orElse(null);

        if (pointData != null) {
            val pos = pointData.getTranPos();
            val rot = pointData.getTranRot();
            int sceneId = pointData.getTranSceneId();

            player.getWorld().transferPlayerToScene(player, sceneId, pos, rot);
            session.send(new PacketPersonalSceneJumpRsp(sceneId, pos));
        }

    }

}
