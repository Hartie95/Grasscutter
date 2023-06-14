package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.net.proto.SetUpAvatarTeamReqOuterClass.SetUpAvatarTeamReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketSetUpAvatarTeamRsp;
import org.jetbrains.annotations.NotNull;

@Opcodes(PacketOpcodes.SetUpAvatarTeamReq)
public class HandlerSetUpAvatarTeamReq extends PacketHandler {

	@Override
	public void handle(@NotNull GameSession session, byte[] header, byte[] payload) throws Exception {
		SetUpAvatarTeamReq req = SetUpAvatarTeamReq.parseFrom(payload);

		boolean result = session.getPlayer().getTeamManager().setupAvatarTeam(
            req.getTeamId(), req.getAvatarTeamGuidListList(), req.getCurAvatarGuid());

        session.getPlayer().sendPacket(new PacketSetUpAvatarTeamRsp(
            req.getTeamId(), req.getCurAvatarGuid(), req.getAvatarTeamGuidListList(),
            result ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE));
	}

}
