package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.home.GameHome;
import emu.grasscutter.game.home.HomeSceneItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.FriendEnterHomeOptionOuterClass;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import emu.grasscutter.net.proto.TryEnterHomeReqOuterClass.TryEnterHomeReq;
import emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketTryEnterHomeRsp;
import emu.grasscutter.utils.Position;

@Opcodes(PacketOpcodes.TryEnterHomeReq)
public class HandlerTryEnterHomeReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        TryEnterHomeReq req = TryEnterHomeReq.parseFrom(payload);
        Player targetPlayer = session.getServer().getPlayerByUid(req.getTargetUid(), true);

        if (req.getTargetUid() != session.getPlayer().getUid()) {
            // I hope that tomorrow there will be a hero who can support multiplayer mode and write code like a poem
            GameHome targetHome = GameHome.getByUid(req.getTargetUid());
            switch (targetHome.getEnterHomeOption()) {
                case FriendEnterHomeOptionOuterClass.FriendEnterHomeOption.FRIEND_ENTER_HOME_OPTION_NEED_CONFIRM_VALUE:
                    if (!targetPlayer.isOnline()) {
                        session.send(new PacketTryEnterHomeRsp(RetcodeOuterClass.Retcode.RET_HOME_OWNER_OFFLINE_VALUE, req.getTargetUid()));
                        return;
                    }
                    break;
                case FriendEnterHomeOptionOuterClass.FriendEnterHomeOption.FRIEND_ENTER_HOME_OPTION_REFUSE_VALUE:
                    session.send(new PacketTryEnterHomeRsp(RetcodeOuterClass.Retcode.RET_HOME_HOME_REFUSE_GUEST_ENTER_VALUE, req.getTargetUid()));
                    return;
                case FriendEnterHomeOptionOuterClass.FriendEnterHomeOption.FRIEND_ENTER_HOME_OPTION_DIRECT_VALUE:
                    break;
            }

            session.send(new PacketTryEnterHomeRsp());
            return;
        }

        GameHome home = session.getPlayer().getHome();

        // prepare the default arrangement for first come in
        HomeSceneItem homeScene = home.getHomeSceneItem();
        home.save();

        Scene scene = session.getPlayer().getWorld().getSceneById(homeScene.getSceneId());
        Position pos = scene.getScriptManager().getConfig().born_pos;

        boolean result = session.getPlayer().getWorld().transferPlayerToScene(session.getPlayer(), homeScene.getSceneId(), pos);
        home.sendInitialPackets();
        if (result) session.send(new PacketTryEnterHomeRsp(req.getTargetUid()));
    }
}
