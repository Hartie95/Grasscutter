package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.QuestUpdateQuestVarReqOuterClass.QuestUpdateQuestVarReq;
import emu.grasscutter.net.proto.QuestVarOpOuterClass.QuestVarOp;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketQuestUpdateQuestVarRsp;
import lombok.val;

import java.util.List;

@Opcodes(PacketOpcodes.QuestUpdateQuestVarReq)
public class HandlerQuestUpdateQuestVarReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        //Client sends packets. One with the value, and one with the index and the new value to set/inc/dec
        val req = QuestUpdateQuestVarReq.parseFrom(payload);
        val questManager = session.getPlayer().getQuestManager();
        val subQuest = questManager.getQuestById(req.getQuestId());
        var mainQuest = questManager.getMainQuestById(req.getParentQuestId());
        if(mainQuest == null && subQuest!= null){
            mainQuest = subQuest.getMainQuest();
        }

        if (mainQuest==null){
            session.send(new PacketQuestUpdateQuestVarRsp(req, Retcode.RET_QUEST_NOT_EXIST));
            Grasscutter.getLogger().error("trying to update QuestVar for non existing quest s{} m{}", req.getQuestId(), req.getParentQuestId());
            return;
        }
        List<QuestVarOp> questVars = req.getQuestVarOpListList();
        val questVarUpdate = mainQuest.getQuestVarsUpdate();
        if (questVarUpdate.size() == 0) {
            for (QuestVarOp questVar : questVars) {
                questVarUpdate.add(questVar.getValue());
            }
        } else {
            for (QuestVarOp questVar : questVars) {
                if (questVar.getIsAdd()) {
                    if (questVar.getValue() >= 0) {
                        mainQuest.incQuestVar(questVar.getIndex(), questVar.getValue());
                    } else {
                        mainQuest.decQuestVar(questVar.getIndex(), questVar.getValue());
                    }
                } else {
                    mainQuest.setQuestVar(questVar.getIndex(), questVarUpdate.get(0));
                }
                //remove the first element from the update list
                questVarUpdate.remove(0);
            }
        }
        session.send(new PacketQuestUpdateQuestVarRsp(req));
    }

}
