package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ClientAbilityChangeNotifyOuterClass.ClientAbilityChangeNotify;
import emu.grasscutter.net.proto.AbilityInvokeEntryOuterClass.AbilityInvokeEntry;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.ClientAbilityChangeNotify)
public class HandlerClientAbilityChangeNotify extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		ClientAbilityChangeNotify notif = ClientAbilityChangeNotify.parseFrom(payload);

		Player player = session.getPlayer();
		for (AbilityInvokeEntry entry : notif.getInvokesList()) {
			player.getAbilityManager().onAbilityInvoke(entry);
			player.getAbilityInvokeHandler().addEntry(entry.getForwardType(), entry);
		}
	}

}
