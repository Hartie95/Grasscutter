package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.net.proto.LevelupCityReqOuterClass.LevelupCityReq;

@Opcodes(PacketOpcodes.LevelupCityReq)
public class HandlerLevelUpCityReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		LevelupCityReq req = LevelupCityReq.parseFrom(payload);

		session.getPlayer().getProgressManager().levelUpCity(req.getSceneId(), req.getAreaId(), req.getItemNum());
	}

}
