package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.home.GameHome;
import emu.grasscutter.game.home.HomeSceneItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeGetArrangementInfoRspOuterClass.HomeGetArrangementInfoRsp;
import emu.grasscutter.net.proto.HomeSceneArrangementInfoOuterClass.HomeSceneArrangementInfo;

import java.util.List;

public class PacketHomeGetArrangementInfoRsp extends BasePacket {

	public PacketHomeGetArrangementInfoRsp(Player player, List<Integer> sceneIdList) {
		super(PacketOpcodes.HomeGetArrangementInfoRsp);

		GameHome home = player.getHome();

		List<HomeSceneArrangementInfo> homeScenes = sceneIdList.stream()
			.map(home::getHomeSceneItem)
			.map(HomeSceneItem::toProto)
			.toList();

		home.save();

		HomeGetArrangementInfoRsp proto = HomeGetArrangementInfoRsp.newBuilder()
			.addAllSceneArrangementInfoList(homeScenes)
			.build();

		this.setData(proto);
	}
}
