package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.net.proto.FurnitureMakeCancelReqOuterClass.FurnitureMakeCancelReq;
import emu.grasscutter.server.packet.send.PacketFurnitureMakeCancelRsp;

@Opcodes(PacketOpcodes.FurnitureMakeCancelReq)
public class HandlerFurnitureMakeCancelReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		FurnitureMakeCancelReq req = FurnitureMakeCancelReq.parseFrom(payload);
		session.getPlayer().getFurnitureManager().cancelMake(req.getIndex(), req.getMakeId());
	}

}
