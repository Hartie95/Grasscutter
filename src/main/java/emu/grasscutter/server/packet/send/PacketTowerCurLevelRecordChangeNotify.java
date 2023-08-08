package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerCurLevelRecordChangeNotifyOuterClass.TowerCurLevelRecordChangeNotify;
import emu.grasscutter.net.proto.TowerCurLevelRecordOuterClass.TowerCurLevelRecord;

public class PacketTowerCurLevelRecordChangeNotify extends BasePacket {

	public PacketTowerCurLevelRecordChangeNotify(TowerCurLevelRecord recordProto) {
		super(PacketOpcodes.TowerCurLevelRecordChangeNotify);

		this.setData(TowerCurLevelRecordChangeNotify.newBuilder()
            .setCurLevelRecord(recordProto)
            .build());
	}
}
