package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.proto.PacketHeadOuterClass.PacketHead;
import emu.grasscutter.net.packet.TypedPacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketPingRsp;
import org.anime_game_servers.multi_proto.gi.messages.other.PingReq;

public class HandlerPingReq extends TypedPacketHandler<PingReq> {

    @Override
    public void handle(GameSession session, byte[] header, PingReq req) throws Exception {
        PacketHead head = PacketHead.parseFrom(header);
        session.updateLastPingTime(req.getClientTime());
        session.send(new PacketPingRsp(head.getClientSequenceId(), req.getClientTime()));
    }

}
