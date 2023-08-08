package emu.grasscutter.server.packet.recv;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScenePointEntry;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.SceneTransToPointReqOuterClass.SceneTransToPointReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketSceneTransToPointRsp;
import lombok.val;

import java.util.Optional;

@Opcodes(PacketOpcodes.SceneTransToPointReq)
public class HandlerSceneTransToPointReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        val req = SceneTransToPointReq.parseFrom(payload);
        val player = session.getPlayer();
        val result = Optional.ofNullable(GameData.getScenePointEntryById(req.getSceneId(), req.getPointId()))
            .map(ScenePointEntry::getPointData)
            .filter(pointData -> player.getWorld().transferPlayerToScene(
                player, req.getSceneId(), TeleportType.WAYPOINT, pointData.getTranPos(), pointData.getTranRot()))
            .isPresent();

        session.send(new PacketSceneTransToPointRsp(result, req.getPointId(), req.getSceneId()));
    }

}
