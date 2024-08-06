package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.quest.entities.QuestTransmitReq;
import org.anime_game_servers.multi_proto.gi.messages.quest.entities.QuestTransmitRsp;

public class PacketQuestTransmitRsp extends BaseTypedPacket<QuestTransmitRsp> {

	public PacketQuestTransmitRsp(boolean result, QuestTransmitReq req) {
        super(new QuestTransmitRsp());
        proto.setPointId(req.getPointId());
        proto.setQuestId(req.getQuestId());
        proto.setRetCode(result ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE);
	}
}
