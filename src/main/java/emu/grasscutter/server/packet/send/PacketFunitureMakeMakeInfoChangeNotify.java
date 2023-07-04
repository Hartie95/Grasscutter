package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.home.GameHome;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.FunitureMakeMakeInfoChangeNotifyOuterClass.FunitureMakeMakeInfoChangeNotify;
import emu.grasscutter.net.proto.FurnitureMakeMakeInfoOuterClass.FurnitureMakeMakeInfo;

public class PacketFunitureMakeMakeInfoChangeNotify extends BasePacket {

	public PacketFunitureMakeMakeInfoChangeNotify(int furnitureId, int makeCount) {
		super(PacketOpcodes.FunitureMakeMakeInfoChangeNotify);
		this.setData(FunitureMakeMakeInfoChangeNotify.newBuilder()
			.setMakeInfo(FurnitureMakeMakeInfo.newBuilder()
				.setFurnitureId(furnitureId)
				.setMakeCount(makeCount)
				.build())
			.build());
	}
}
