package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.GetHomeLevelUpRewardReqOuterClass.GetHomeLevelUpRewardReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketGetHomeLevelUpRewardRsp;

@Opcodes(PacketOpcodes.GetHomeLevelUpRewardReq)
public class HandlerGetHomeLevelUpRewardReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		GetHomeLevelUpRewardReq req = GetHomeLevelUpRewardReq.parseFrom(payload);
		session.send(new PacketGetHomeLevelUpRewardRsp(session.getPlayer(), req.getLevel()));
	}

}
