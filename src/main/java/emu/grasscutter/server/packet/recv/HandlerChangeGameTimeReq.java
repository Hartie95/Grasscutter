package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.quest.enums.QuestCond;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ChangeGameTimeReqOuterClass.ChangeGameTimeReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketChangeGameTimeRsp;

@Opcodes(PacketOpcodes.ChangeGameTimeReq)
public class HandlerChangeGameTimeReq extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		ChangeGameTimeReq req = ChangeGameTimeReq.parseFrom(payload);

		session.getPlayer().getScene().changeTime(req.getGameTime());
		session.getPlayer().getQuestManager().queueEvent(QuestCond.QUEST_COND_IS_DAYTIME, 
			(req.getGameTime() / 60) >= 6 && (req.getGameTime() / 60) <= 19 ? 1 : 0);
        session.getPlayer().getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_GAME_TIME_TICK,
            String.valueOf(req.getGameTime() / 60), // hours
			req.getExtraDays()); //days
		session.getPlayer().sendPacket(new PacketChangeGameTimeRsp(session.getPlayer()));
	}

}
