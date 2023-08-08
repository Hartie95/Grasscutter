package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeSceneJumpReqOuterClass.HomeSceneJumpReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketHomeSceneJumpRsp;
import lombok.val;

@Opcodes(PacketOpcodes.HomeSceneJumpReq)
public class HandlerHomeSceneJumpReq extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		val req = HomeSceneJumpReq.parseFrom(payload);

		int realmId = 2000 + session.getPlayer().getCurrentRealmId();
        val home = session.getPlayer().getHome();
        val homeScene = home.getHomeSceneItem(realmId);
		home.save();

        // the function should be able to get pos and rot from scriptManager config
		session.getPlayer().getWorld().transferPlayerToScene(
            session.getPlayer(),
            req.getIsEnterRoomScene() ? homeScene.getRoomSceneId() : realmId,
            null, null
		);

		session.send(new PacketHomeSceneJumpRsp(req.getIsEnterRoomScene()));
	}

}
