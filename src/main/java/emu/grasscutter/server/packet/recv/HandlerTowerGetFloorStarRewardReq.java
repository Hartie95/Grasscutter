package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerGetFloorStarRewardReqOuterClass.TowerGetFloorStarRewardReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketTowerGetFloorStarRewardRsp;

@Opcodes(PacketOpcodes.TowerGetFloorStarRewardReq)
public class HandlerTowerGetFloorStarRewardReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        TowerGetFloorStarRewardReq req = TowerGetFloorStarRewardReq.parseFrom(payload);

        session.send(new PacketTowerGetFloorStarRewardRsp(
            session.getPlayer().getTowerManager().getStarReward(req.getFloorId()), req.getFloorId()));
    }
}
