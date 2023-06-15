package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetAllMailNotifyOuterClass.GetAllMailNotify;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketGetAllMailResultNotify;
import emu.grasscutter.server.packet.send.PacketGetAllMailRsp;
import lombok.val;

/**
 * Client request for the mail, used by 3.0 and higher to request the mail
 */
@Opcodes(PacketOpcodes.GetAllMailNotify)
public class HandlerGetAllMailNotify extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        val req = GetAllMailNotify.parseFrom(payload);
        session.send(new PacketGetAllMailResultNotify(session.getPlayer(), req.getIsCollected()));
    }
}
