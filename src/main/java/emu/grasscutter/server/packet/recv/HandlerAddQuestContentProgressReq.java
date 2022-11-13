package emu.grasscutter.server.packet.recv;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.enums.QuestCond;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AddQuestContentProgressReqOuterClass;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketAddQuestContentProgressRsp;
import java.util.List;
import java.util.stream.Stream;

@Opcodes(PacketOpcodes.AddQuestContentProgressReq)
public class HandlerAddQuestContentProgressReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        var req = AddQuestContentProgressReqOuterClass.AddQuestContentProgressReq.parseFrom(payload);
        //Find all conditions in quest that are the same as the given one
        Stream<QuestData.QuestAcceptCondition> acceptCond = GameData.getQuestDataMap().get(req.getParam()).getAcceptCond().stream();
        Stream<QuestData.QuestContentCondition> finishCond = GameData.getQuestDataMap().get(req.getParam()).getFinishCond().stream();
        Stream<QuestData.QuestContentCondition> failCond = GameData.getQuestDataMap().get(req.getParam()).getFailCond().stream();
        List<QuestData.QuestContentCondition> allCondMatch = Stream.concat(failCond,finishCond).filter(p -> p.getType().getValue() == req.getContentType()).toList();
        for (QuestData.QuestContentCondition cond : allCondMatch ) {
            session.getPlayer().getQuestManager().queueEvent(QuestContent.getContentTriggerByValue(req.getContentType()), cond.getParam());
        }
        for (QuestData.QuestAcceptCondition cond : acceptCond.toList() ) {
            session.getPlayer().getQuestManager().queueEvent(QuestCond.getContentTriggerByValue(req.getContentType()), cond.getParam());
        }
        session.send(new PacketAddQuestContentProgressRsp(req.getContentType()));
    }

}
