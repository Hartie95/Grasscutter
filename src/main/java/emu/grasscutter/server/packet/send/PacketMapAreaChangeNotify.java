package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.MapAreaInfoItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.MapAreaChangeNotifyOuterClass.MapAreaChangeNotify;

public class PacketMapAreaChangeNotify extends BasePacket {
	
	public PacketMapAreaChangeNotify(Player player) {
		super(PacketOpcodes.MapAreaChangeNotify);
		this.setData(MapAreaChangeNotify.newBuilder()
			.addAllMapAreaInfoList(player.getMapAreaInfoMap().values().stream()
				.map(MapAreaInfoItem::toProto)
				.toList())
			.build());
	}
}
