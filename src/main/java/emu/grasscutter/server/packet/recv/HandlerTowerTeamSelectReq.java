package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerTeamSelectReqOuterClass.TowerTeamSelectReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketTowerTeamSelectRsp;

@Opcodes(PacketOpcodes.TowerTeamSelectReq)
public class HandlerTowerTeamSelectReq extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		TowerTeamSelectReq req = TowerTeamSelectReq.parseFrom(payload);

		session.send(new PacketTowerTeamSelectRsp(
            session.getPlayer().getTowerManager().teamSelect(req.getFloorId(), req.getTowerTeamListList())));
	}
}
