package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.home.GameHome;
import emu.grasscutter.game.home.HomeSceneItem;
import emu.grasscutter.game.home.HomeBlockItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeMarkPointNotifyOuterClass.HomeMarkPointNotify;
import emu.grasscutter.net.proto.HomeMarkPointSceneDataOuterClass.HomeMarkPointSceneData;

import java.util.Collection;

public class PacketHomeMarkPointNotify extends BasePacket {

	public PacketHomeMarkPointNotify(Player player) {
		super(PacketOpcodes.HomeMarkPointNotify);

		if(player.getRealmList() == null || player.getCurrentRealmId() <= 0) return;

		HomeMarkPointNotify.Builder proto = HomeMarkPointNotify.newBuilder();

		HomeSceneItem homeScene = player.getHome().getHomeSceneItem();

		HomeMarkPointSceneData.Builder markPointDataHomeScene = HomeMarkPointSceneData.newBuilder()
			.setModuleId(player.getCurrentRealmId())
			.setSceneId(homeScene.getSceneId())
			.setTeapotSpiritPos(homeScene.getDjinnPos().toProto());

		homeScene.getBlockItems().values().stream()
			.map(HomeBlockItem::getDeployFurnitureList).forEach(block -> {
				block.forEach(furniture -> {
					if (furniture == null) return;
					markPointDataHomeScene.addFurnitureList(furniture.toMarkPointProto(player));
				});
			});
			
		proto.addMarkPointDataList(markPointDataHomeScene.build());

		HomeSceneItem roomScene = player.getHome().getHomeSceneItem(homeScene.getRoomSceneId());

		HomeMarkPointSceneData.Builder markPointDataRoomScene = HomeMarkPointSceneData.newBuilder()
			.setModuleId(player.getCurrentRealmId())
			.setSceneId(roomScene.getSceneId());

		roomScene.getBlockItems().values().stream()
			.map(HomeBlockItem::getDeployFurnitureList).forEach(block -> {
				block.forEach(furniture -> {
					if (furniture == null || !furniture.isNPC()) return;
					markPointDataRoomScene.addFurnitureList(furniture.toMarkPointProto(player));
				});
			});
		
			
		proto.addMarkPointDataList(markPointDataRoomScene.build());

		this.setData(proto.build());
	}
}
