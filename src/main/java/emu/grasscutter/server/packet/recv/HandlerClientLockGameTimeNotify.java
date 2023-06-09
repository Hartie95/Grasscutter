package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.ClientLockGameTimeNotifyOuterClass.ClientLockGameTimeNotify;
import emu.grasscutter.server.game.GameSession;
import lombok.val;

@Opcodes(PacketOpcodes.ClientLockGameTimeNotify)
public class HandlerClientLockGameTimeNotify extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		val req = ClientLockGameTimeNotify.parseFrom(payload);
        Grasscutter.getLogger().error("LOCKING GAMETIME: {}", req.getIsLock());

		//session.getPlayer().getWorld().setGameTimeLocked(req.getIsLock());
	}

}
