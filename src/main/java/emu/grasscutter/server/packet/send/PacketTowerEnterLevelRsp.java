package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.tower.TowerData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerEnterLevelRspOuterClass.TowerEnterLevelRsp;

public class PacketTowerEnterLevelRsp extends BasePacket {

	public PacketTowerEnterLevelRsp(TowerData data) {
		super(PacketOpcodes.TowerEnterLevelRsp);

		this.setData(TowerEnterLevelRsp.newBuilder()
            .setFloorId(data.getCurrentFloorId())
            .setLevelIndex(data.getCurrentLevelIndex())
            .addAllTowerBuffIdList(data.getRandomBuffs())
            .build());
	}
}
