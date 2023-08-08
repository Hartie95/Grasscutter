package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonInterruptChallengeReqOuterClass.DungeonInterruptChallengeReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketDungeonInterruptChallengeRsp;

import java.util.Optional;

@Opcodes(PacketOpcodes.DungeonInterruptChallengeReq)
public class HandlerDungeonInterruptChallengeReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        DungeonInterruptChallengeReq req = DungeonInterruptChallengeReq.parseFrom(payload);

        session.getPlayer().sendPacket(new PacketDungeonInterruptChallengeRsp(
            Optional.ofNullable(session.getPlayer().getScene().getChallenge())
                .filter(c -> c.isThisChallenge(req.getChallengeIndex(), req.getChallengeId(), req.getGroupId()))
                .map(WorldChallenge::fail)
                .isPresent(), req));
    }
}
