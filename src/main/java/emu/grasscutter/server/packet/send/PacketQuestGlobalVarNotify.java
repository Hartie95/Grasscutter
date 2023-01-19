package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.QuestGlobalVarNotifyOuterClass.QuestGlobalVarNotify;
import emu.grasscutter.net.proto.QuestGlobalVarOuterClass.QuestGlobalVar;


public class PacketQuestGlobalVarNotify extends BasePacket {
	
	public PacketQuestGlobalVarNotify(Player player) {
		super(PacketOpcodes.QuestGlobalVarNotify);
		this.setData(QuestGlobalVarNotify.newBuilder()
			.addAllVarList(player.getQuestGlobalVariables().entrySet().stream()
				.map(e -> QuestGlobalVar.newBuilder()
					.setKey(e.getKey())
					.setValue(e.getValue())
					.build())
				.toList())
			.build());
	}	
}
