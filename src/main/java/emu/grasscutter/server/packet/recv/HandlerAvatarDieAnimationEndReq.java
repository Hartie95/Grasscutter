package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AvatarDieAnimationEndReqOuterClass.AvatarDieAnimationEndReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketAvatarDieAnimationEndRsp;

@Opcodes(PacketOpcodes.AvatarDieAnimationEndReq)
public class HandlerAvatarDieAnimationEndReq extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		AvatarDieAnimationEndReq req = AvatarDieAnimationEndReq.parseFrom(payload);

        // TODO There is also reborn pos being sent, which might be helpful
		session.getPlayer().getTeamManager().onAvatarDie(req.getDieGuid());
        // Response packet
        session.getPlayer().sendPacket(new PacketAvatarDieAnimationEndRsp(req.getDieGuid(), req.getSkillId()));
	}

}
