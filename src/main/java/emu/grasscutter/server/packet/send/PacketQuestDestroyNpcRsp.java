package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.quest.entities.QuestDestroyNpcRsp;

public class PacketQuestDestroyNpcRsp extends BaseTypedPacket<QuestDestroyNpcRsp> {

    public PacketQuestDestroyNpcRsp(int npcId, int parentQuestId, int retCode) {
        super(new QuestDestroyNpcRsp(), true);
        proto.setNpcId(npcId);
        proto.setParentQuestId(parentQuestId);
        proto.setRetCode(retCode);
    }
}
