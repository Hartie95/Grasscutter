package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketTowerSurrenderRsp;

@Opcodes(PacketOpcodes.TowerSurrenderReq)
public class HandlerTowerSurrenderReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        session.getPlayer().getTowerData().reset();
        session.getPlayer().getTowerManager().notifyCurRecordChange();
        session.send(new PacketTowerSurrenderRsp());
    }
}
