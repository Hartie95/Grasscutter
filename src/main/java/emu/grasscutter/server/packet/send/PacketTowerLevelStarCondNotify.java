package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.tower.TowerData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerLevelStarCondDataOuterClass.TowerLevelStarCondData;
import emu.grasscutter.net.proto.TowerLevelStarCondNotifyOuterClass.TowerLevelStarCondNotify;

public class PacketTowerLevelStarCondNotify extends BasePacket {

	public PacketTowerLevelStarCondNotify(TowerData data) {
		super(PacketOpcodes.TowerLevelStarCondNotify);

		TowerLevelStarCondNotify proto = TowerLevelStarCondNotify.newBuilder()
				.setFloorId(data.getCurrentFloorId())
				.setLevelIndex(data.getCurrentLevelIndex())
				.addCondDataList(TowerLevelStarCondData.newBuilder()
						.setCondValue(1)
						.build())
				.addCondDataList(TowerLevelStarCondData.newBuilder()
						.setCondValue(2)
						.build())
				.addCondDataList(TowerLevelStarCondData.newBuilder()
						.setCondValue(3)
						.build())
				.build();

		this.setData(proto);
	}
}
