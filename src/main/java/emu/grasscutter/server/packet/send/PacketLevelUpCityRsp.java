package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.LevelupCityRspOuterClass.LevelupCityRsp;
import emu.grasscutter.net.proto.CityInfoOuterClass.CityInfo;

public class PacketLevelUpCityRsp extends BasePacket {
	
	public PacketLevelUpCityRsp(int areaId, int sceneId, CityInfo cityInfo) {
		super(PacketOpcodes.LevelupCityRsp);
		this.setData(LevelupCityRsp.newBuilder()
			.setAreaId(areaId)
			.setSceneId(sceneId)
			.setCityInfo(cityInfo)
			.build());
	}
}
