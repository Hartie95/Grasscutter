package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ChangeTeamNameReqOuterClass.ChangeTeamNameReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketChangeTeamNameRsp;
import org.jetbrains.annotations.NotNull;

@Opcodes(PacketOpcodes.ChangeTeamNameReq)
public class HandlerChangeTeamNameReq extends PacketHandler {

	@Override
	public void handle(@NotNull GameSession session, byte[] header, byte[] payload) throws Exception {
		ChangeTeamNameReq req = ChangeTeamNameReq.parseFrom(payload);

		boolean result = session.getPlayer().getTeamManager().setTeamName(req.getTeamId(), req.getTeamName());

        session.getPlayer().sendPacket(new PacketChangeTeamNameRsp(
            result ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE,
            req.getTeamId(), req.getTeamName()));
	}

}
