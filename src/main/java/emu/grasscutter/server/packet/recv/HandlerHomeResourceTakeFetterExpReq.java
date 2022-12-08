package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.HomeResourceTakeFetterExpReqOuterClass.HomeResourceTakeFetterExpReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketHomeResourceTakeFetterExpRsp;


@Opcodes(PacketOpcodes.HomeResourceTakeFetterExpReq)
public class HandlerHomeResourceTakeFetterExpReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		session.send(new PacketHomeResourceTakeFetterExpRsp(session.getPlayer()));
	}

}
