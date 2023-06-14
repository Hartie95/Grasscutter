package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketAddCustomTeamRsp;
import emu.grasscutter.server.packet.send.PacketCustomTeamListNotify;
import org.jetbrains.annotations.NotNull;

@Opcodes(PacketOpcodes.AddCustomTeamReq)
public class HandlerAddCustomTeamReq extends PacketHandler {
    @Override
    public void handle(@NotNull GameSession session, byte[] header, byte[] payload) throws Exception {
        boolean result = session.getPlayer().getTeamManager().addNewCustomTeam();
        // Send packets.
        if (result) {
            session.getPlayer().sendPacket(new PacketCustomTeamListNotify(session.getPlayer()));
        }
        session.getPlayer().sendPacket(new PacketAddCustomTeamRsp(result ? Retcode.RET_SUCC : Retcode.RET_FAIL));
    }
}
