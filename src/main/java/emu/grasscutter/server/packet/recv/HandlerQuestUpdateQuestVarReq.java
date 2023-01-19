package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.quest.GameMainQuest;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.QuestUpdateQuestVarReqOuterClass.QuestUpdateQuestVarReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketQuestUpdateQuestVarRsp;

import java.util.List;

@Opcodes(PacketOpcodes.QuestUpdateQuestVarReq)
public class HandlerQuestUpdateQuestVarReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        //Client sends packets. One with the value, and one with the index and the new value to set/inc/dec
        QuestUpdateQuestVarReq req = QuestUpdateQuestVarReq.parseFrom(payload);
        GameMainQuest mainQuest = session.getPlayer().getQuestManager().getMainQuestById(req.getQuestId()/100);

        req.getQuestVarOpListList().forEach(questVar -> {
            if (questVar.getIsAdd()) {
                mainQuest.incQuestVar(questVar.getIndex(), questVar.getValue());
            }
            mainQuest.decQuestVar(questVar.getIndex(), questVar.getValue());
        });
        session.send(new PacketQuestUpdateQuestVarRsp(req.getQuestId()));
    }

}
