package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.net.proto.GetMapAreaReqOuterClass.GetMapAreaReq;
import emu.grasscutter.net.proto.GetMapAreaRspOuterClass.GetMapAreaRsp;
import emu.grasscutter.server.packet.send.PacketGetMapAreaRsp;

@Opcodes(PacketOpcodes.GetMapAreaReq)
public class HandlerGetMapAreaReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		// Auto template

		session.send(new PacketGetMapAreaRsp(session.getPlayer()));
	}

}
