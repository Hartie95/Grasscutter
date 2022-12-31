package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.QuestGlobalVarNotifyOuterClass.QuestGlobalVarNotify;
import emu.grasscutter.net.proto.QuestGlobalVarOuterClass.QuestGlobalVar;

import java.util.List;

public class PacketQuestGlobalVarNotify extends BasePacket {
	
	public PacketQuestGlobalVarNotify(Player player) {
		super(PacketOpcodes.QuestGlobalVarNotify);
		QuestGlobalVarNotify.Builder proto = QuestGlobalVarNotify.newBuilder();
		player.getQuestGlobalVariables().entrySet().stream()
			.filter(e -> e.getValue() > 0)
			.forEach(e -> proto.addVarList(QuestGlobalVar.newBuilder()
				.setKey(e.getKey())
				.setValue(e.getValue())
				.build()));
		this.setData(proto.build());
	}
}
