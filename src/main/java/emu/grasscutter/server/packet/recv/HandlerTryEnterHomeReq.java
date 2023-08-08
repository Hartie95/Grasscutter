package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.home.GameHome;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TryEnterHomeReqOuterClass.TryEnterHomeReq;
import emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketTryEnterHomeRsp;
import lombok.val;

import static emu.grasscutter.net.proto.FriendEnterHomeOptionOuterClass.FriendEnterHomeOption.*;
import static emu.grasscutter.net.proto.RetcodeOuterClass.Retcode.*;

@Opcodes(PacketOpcodes.TryEnterHomeReq)
public class HandlerTryEnterHomeReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        val req = TryEnterHomeReq.parseFrom(payload);
        val targetPlayer = session.getServer().getPlayerByUid(req.getTargetUid(), true);

        if (req.getTargetUid() != session.getPlayer().getUid() && targetPlayer != null) {
            // I hope that tomorrow there will be a hero who can support multiplayer mode and write code like a poem
            val targetHome = GameHome.getByUid(req.getTargetUid());
            switch (targetHome.getEnterHomeOption()) {
                case FRIEND_ENTER_HOME_OPTION_NEED_CONFIRM_VALUE -> {
                    if (targetPlayer.isOnline()) break;

                    session.send(new PacketTryEnterHomeRsp(RET_HOME_OWNER_OFFLINE_VALUE, req.getTargetUid()));
                }
                case FRIEND_ENTER_HOME_OPTION_REFUSE_VALUE ->
                    session.send(new PacketTryEnterHomeRsp(RET_HOME_HOME_REFUSE_GUEST_ENTER_VALUE, req.getTargetUid()));
                case FRIEND_ENTER_HOME_OPTION_DIRECT_VALUE -> session.send(new PacketTryEnterHomeRsp());
            }
            return;
        }

        int realmId = 2000 + session.getPlayer().getCurrentRealmId();
        val home = session.getPlayer().getHome();

        // prepare the default arrangement for first come in
        home.getHomeSceneItem(realmId);
        home.save();

        // the function should be able to get pos and rot from scriptManager config
        boolean result = session.getPlayer().getWorld().transferPlayerToScene(
            session.getPlayer(), realmId, TeleportType.WAYPOINT, null, null);

        if (result) session.send(new PacketTryEnterHomeRsp(req.getTargetUid()));
    }
}
