package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeUpdateArrangementInfoReqOuterClass.HomeUpdateArrangementInfoReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.*;

@Opcodes(PacketOpcodes.HomeUpdateArrangementInfoReq)
public class HandlerHomeUpdateArrangementInfoReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		HomeUpdateArrangementInfoReq req = HomeUpdateArrangementInfoReq.parseFrom(payload);

		session.getPlayer().getHome().updateHomeSceneBlock(req.getSceneArrangementInfo());
		session.getPlayer().getHome().save();
		session.send(new PacketSceneTimeNotify(session.getPlayer()));
		session.send(new PacketHomePlantInfoNotify(session.getPlayer()));
		// PacketHomeFishFarmingInfoNotify
		// PacketHomeAvatarRewardEventNotify
		// PacketHomeAvatarTalkFinishInfoNotify
		// PacketHomeAvatarSummonAllEventNotify
		// PacketFurnitureCurModuleArrangeCountNotify
		session.send(new PacketHomeComfortInfoNotify(session.getPlayer()));
		session.send(new PacketHomeMarkPointNotify(session.getPlayer()));
		// PacketHomePriorCheckNotify
		session.send(new PacketHomeUpdateArrangementInfoRsp());
	}

}
