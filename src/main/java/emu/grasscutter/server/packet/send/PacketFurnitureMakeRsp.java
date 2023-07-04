package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.home.GameHome;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.FurnitureMakeRspOuterClass.FurnitureMakeRsp;
import emu.grasscutter.net.proto.FurnitureMakeSlotOuterClass.FurnitureMakeSlot;

public class PacketFurnitureMakeRsp extends BasePacket {

	public PacketFurnitureMakeRsp(Player player) {
		super(PacketOpcodes.FurnitureMakeRsp);

		FurnitureMakeRsp.Builder proto = FurnitureMakeRsp.newBuilder();

		proto.setFurnitureMakeSlot(FurnitureMakeSlot.newBuilder()
				.addAllFurnitureMakeDataList(player.getHome().furnitureMakeItemToProto())
				.build());

		proto.addAllMakeInfoList(player.madeFurnitureProto());

		this.setData(proto.build());
	}
}
