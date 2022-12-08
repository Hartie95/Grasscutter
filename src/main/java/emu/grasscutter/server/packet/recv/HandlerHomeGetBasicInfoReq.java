package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketHomeBasicInfoNotify;
import emu.grasscutter.server.packet.send.PacketHomeComfortInfoNotify;

@Opcodes(PacketOpcodes.HomeGetBasicInfoReq)
public class HandlerHomeGetBasicInfoReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		// HomePriorCheckNotify
		session.send(new PacketHomeBasicInfoNotify(session.getPlayer(), false));
		session.send(new PacketHomeComfortInfoNotify(session.getPlayer()));
	}

}
