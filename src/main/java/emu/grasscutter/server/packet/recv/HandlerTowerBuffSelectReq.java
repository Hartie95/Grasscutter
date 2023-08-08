package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerBuffSelectReqOuterClass.TowerBuffSelectReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketTowerBuffSelectRsp;

@Opcodes(PacketOpcodes.TowerBuffSelectReq)
public class HandlerTowerBuffSelectReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        TowerBuffSelectReq req = TowerBuffSelectReq.parseFrom(payload);

        session.getPlayer().getTowerManager().addBuffs(req.getTowerBuffId());
        session.send(new PacketTowerBuffSelectRsp(req.getTowerBuffId()));
    }
}
