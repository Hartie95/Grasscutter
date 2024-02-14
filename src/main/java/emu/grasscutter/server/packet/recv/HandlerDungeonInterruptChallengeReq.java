package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.net.packet.TypedPacketHandler;
import emu.grasscutter.server.packet.send.PacketDungeonInterruptChallengeRsp;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.challenge.DungeonInterruptChallengeReq;
import emu.grasscutter.server.game.GameSession;
import lombok.val;

import java.util.Optional;

public class HandlerDungeonInterruptChallengeReq extends TypedPacketHandler<DungeonInterruptChallengeReq> {
    @Override
    public void handle(GameSession session, byte[] header, DungeonInterruptChallengeReq req) throws Exception {
        session.getPlayer().sendPacket(new PacketDungeonInterruptChallengeRsp(
            Optional.ofNullable(session.getPlayer().getScene().getChallenge())
                .filter(c -> c.isThisChallenge(req.getChallengeIndex(), req.getChallengeId(), req.getGroupId()))
                .map(WorldChallenge::fail)
                .orElse(false), req.getChallengeId(), req.getChallengeIndex(), req.getGroupId()));

        /*val data = DungeonInterruptChallengeReq.parseFrom(payload);
        val challenge = session.getPlayer().getScene().getChallenge();
        if(challenge!=null && data.getGroupId() == challenge.getGroupId() &&
            challenge.getChallengeId() == data.getChallengeId() &&
            challenge.getChallengeIndex() == data.getChallengeIndex()) {
            challenge.fail();
            session.getPlayer().getScene().broadcastPacket(new PackageDungeonInterruptChallengeRsp(challenge));
        } else {
            session.getPlayer().getScene().broadcastPacket(
                new PacketDungeonInterruptChallengeRsp(Retcode.RET_UNKNOWN_ERROR.getNumber(), data));
        }*/
    }
}
