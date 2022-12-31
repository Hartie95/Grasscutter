package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetAreaExplorePointRspOuterClass.GetAreaExplorePointRsp;

import java.util.List;

public class PacketGetAreaExplorePointRsp extends BasePacket {
	
	public PacketGetAreaExplorePointRsp(List<Integer> areaIdList) {
		super(PacketOpcodes.GetAreaExplorePointRsp);
		// TODO, its the percentage shown in the bottom right corner of the map
		// currently no resource has information about it
		// probably has to do our own calculation with picking up chest and oculus
		this.setData(GetAreaExplorePointRsp.newBuilder()
			.addAllExplorePointList(areaIdList.stream().map(x -> {return 99999;}).toList())
			.addAllAreaIdList(areaIdList)
			.build());
	}
}
