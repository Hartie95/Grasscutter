package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.HomePlantWeedReqOuterClass.HomePlantWeedReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketHomePlantFieldNotify;
import emu.grasscutter.server.packet.send.PacketHomePlantWeedRsp;

@Opcodes(PacketOpcodes.HomePlantWeedReq)
public class HandlerHomePlantWeedReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		HomePlantWeedReq req = HomePlantWeedReq.parseFrom(payload);

		try {
			session.getPlayer().getHome().getHomePlantItem()
				.getFieldByGuid(req.getFieldGuid())
				.getSubFieldByIndex(req.getIndex())
				.reset();
		} catch (Exception e) {
			Grasscutter.getLogger().error("Error resetting status for field: {}", req.getFieldGuid());
			return;
		}

		session.send(new PacketHomePlantFieldNotify(session.getPlayer(), req.getFieldGuid()));
		session.send(new PacketHomePlantWeedRsp(Retcode.RET_SUCC));
	}

}
