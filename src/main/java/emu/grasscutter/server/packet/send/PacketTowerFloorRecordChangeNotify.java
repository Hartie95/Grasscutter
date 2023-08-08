package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.tower.TowerData;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerFloorRecordChangeNotifyOuterClass.TowerFloorRecordChangeNotify;
import emu.grasscutter.net.proto.TowerFloorRecordOuterClass.TowerFloorRecord;

import java.util.Optional;

public class PacketTowerFloorRecordChangeNotify extends BasePacket {

	public PacketTowerFloorRecordChangeNotify(TowerData.TowerFloorRecordInfo info, boolean canEnterScheduleFloor) {
		super(PacketOpcodes.TowerFloorRecordChangeNotify);

		this.setData(TowerFloorRecordChangeNotify.newBuilder()
            .addTowerFloorRecordList(Optional.ofNullable(info).map(TowerData.TowerFloorRecordInfo::toProto)
                .orElse(TowerFloorRecord.newBuilder().build()))
            .setIsFinishedEntranceFloor(canEnterScheduleFloor)
            .build());
	}
}
