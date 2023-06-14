package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ChangeMpTeamAvatarReqOuterClass.ChangeMpTeamAvatarReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketChangeMpTeamAvatarRsp;
import org.jetbrains.annotations.NotNull;

@Opcodes(PacketOpcodes.ChangeMpTeamAvatarReq)
public class HandlerChangeMpTeamAvatarReq extends PacketHandler {

	@Override
	public void handle(@NotNull GameSession session, byte[] header, byte[] payload) throws Exception {
		ChangeMpTeamAvatarReq req = ChangeMpTeamAvatarReq.parseFrom(payload);

		boolean result = session.getPlayer().getTeamManager().setupMpTeam(req.getAvatarGuidListList(), req.getCurAvatarGuid());
        new PacketChangeMpTeamAvatarRsp(
            result ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE,
            req.getAvatarGuidListList(), req.getCurAvatarGuid());
	}

}
