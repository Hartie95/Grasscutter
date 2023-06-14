package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ChooseCurAvatarTeamReqOuterClass.ChooseCurAvatarTeamReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketChooseCurAvatarTeamRsp;
import org.jetbrains.annotations.NotNull;

@Opcodes(PacketOpcodes.ChooseCurAvatarTeamReq)
public class HandlerChooseCurAvatarTeamReq extends PacketHandler {

	@Override
	public void handle(@NotNull GameSession session, byte[] header, byte[] payload) throws Exception {
		ChooseCurAvatarTeamReq req = ChooseCurAvatarTeamReq.parseFrom(payload);

		boolean result = session.getPlayer().getTeamManager().setCurrentTeam(req.getTeamId());
        session.getPlayer().sendPacket(new PacketChooseCurAvatarTeamRsp(
            result ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE,
            req.getTeamId()));

	}

}
