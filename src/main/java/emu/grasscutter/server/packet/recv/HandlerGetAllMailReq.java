package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.GetAllMailReqOuterClass.GetAllMailReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketGetAllMailRsp;
import lombok.val;

/**
 * Client request for the mail, used by 2.8 and lower to request the mail
 */
@Opcodes(PacketOpcodes.GetAllMailReq)
public class HandlerGetAllMailReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        val req = GetAllMailReq.parseFrom(payload);
        session.send(new PacketGetAllMailRsp(session.getPlayer(), req.getIsCollected()));
    }
}
