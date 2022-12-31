package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.MapAreaInfoItem;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetMapAreaRspOuterClass.GetMapAreaRsp;

public class PacketGetMapAreaRsp extends BasePacket {
	
	public PacketGetMapAreaRsp(Player player) {
		super(PacketOpcodes.GetMapAreaRsp);
		GetMapAreaRsp.Builder proto = GetMapAreaRsp.newBuilder();

		GameData.getMapAreaDataMap().values().forEach(e -> {
			proto.addMapAreaInfoList(player.getMapAreaInfoMap().computeIfAbsent(
				e.getId(), 
				s -> MapAreaInfoItem.create(e.getId(), e.getState() != null)
			).toProto());
		});
		this.setData(proto.build());
	}
}
