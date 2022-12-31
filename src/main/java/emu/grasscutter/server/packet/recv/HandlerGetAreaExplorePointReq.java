package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketGetAreaExplorePointRsp;
import emu.grasscutter.net.proto.GetAreaExplorePointReqOuterClass.GetAreaExplorePointReq;

@Opcodes(PacketOpcodes.GetAreaExplorePointReq)
public class HandlerGetAreaExplorePointReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		GetAreaExplorePointReq req = GetAreaExplorePointReq.parseFrom(payload);
		session.send(new PacketGetAreaExplorePointRsp(req.getAreaIdListList()));
	}

}
