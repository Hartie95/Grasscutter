package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.RemoveCustomTeamReqOuterClass.RemoveCustomTeamReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketCustomTeamListNotify;
import emu.grasscutter.server.packet.send.PacketRemoveCustomTeamRsp;
import org.jetbrains.annotations.NotNull;

@Opcodes(PacketOpcodes.RemoveCustomTeamReq)
public class HandlerRemoveCustomTeamReq extends PacketHandler {
    @Override
    public void handle(@NotNull GameSession session, byte[] header, byte[] payload) throws Exception {
        RemoveCustomTeamReq req = RemoveCustomTeamReq.parseFrom(payload);
        boolean result = session.getPlayer().getTeamManager().removeCustomTeam(req.getId());

        // Send packets.
        if (result) {
            session.getPlayer().sendPacket(new PacketCustomTeamListNotify(session.getPlayer()));
        }
        session.getPlayer().sendPacket(new PacketRemoveCustomTeamRsp(
            result ? Retcode.RET_SUCC : Retcode.RET_FAIL,
            req.getId()));
    }
}
