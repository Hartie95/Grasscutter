package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AddQuestContentProgressReqOuterClass.AddQuestContentProgressReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketAddQuestContentProgressRsp;
import lombok.val;

@Opcodes(PacketOpcodes.AddQuestContentProgressReq)
public class HandlerAddQuestContentProgressReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        AddQuestContentProgressReq req = AddQuestContentProgressReq.parseFrom(payload);

        session.getPlayer().getQuestManager().queueEvent(
            QuestContent.getContentTriggerByValue(req.getContentType()), 
            req.getParam());
        session.send(new PacketAddQuestContentProgressRsp(req.getContentType()));
    }

}
