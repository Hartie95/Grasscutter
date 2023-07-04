package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.HomePlantSeedReqOuterClass.HomePlantSeedReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketHomePlantFieldNotify;
import emu.grasscutter.server.packet.send.PacketHomePlantSeedRsp;

@Opcodes(PacketOpcodes.HomePlantSeedReq)
public class HandlerHomePlantSeedReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		HomePlantSeedReq req = HomePlantSeedReq.parseFrom(payload);
		session.send(new PacketHomePlantFieldNotify(session.getPlayer(), req.getSeedIdListList(), req.getFieldGuid(), req.getIndex()));
		session.send(new PacketHomePlantSeedRsp(session.getPlayer(), Retcode.RET_SUCC));
	}

}
