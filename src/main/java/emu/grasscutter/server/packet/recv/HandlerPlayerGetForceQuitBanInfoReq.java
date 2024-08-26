package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.TypedPacketHandler;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketPlayerGetForceQuitBanInfoRsp;
import org.anime_game_servers.multi_proto.gi.messages.unsorted.second.PlayerGetForceQuitBanInfoReq;

public class HandlerPlayerGetForceQuitBanInfoReq extends TypedPacketHandler<PlayerGetForceQuitBanInfoReq> {
    @Override
    public void handle(GameSession session, byte[] header, PlayerGetForceQuitBanInfoReq req) throws Exception {
        if (session.getServer().getMultiplayerSystem().leaveCoop(session.getPlayer())) {
            // Success
            session.send(new PacketPlayerGetForceQuitBanInfoRsp(RetcodeOuterClass.Retcode.RET_SUCC_VALUE));
        } else {
            // Fail
            session.send(new PacketPlayerGetForceQuitBanInfoRsp(RetcodeOuterClass.Retcode.RET_SVR_ERROR_VALUE));
        }
    }
}
