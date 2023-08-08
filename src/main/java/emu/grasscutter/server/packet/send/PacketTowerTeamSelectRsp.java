package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerTeamSelectRspOuterClass.TowerTeamSelectRsp;

public class PacketTowerTeamSelectRsp extends BasePacket {

	public PacketTowerTeamSelectRsp(boolean result) {
		super(PacketOpcodes.TowerTeamSelectRsp);

		this.setData(TowerTeamSelectRsp.newBuilder()
            .setRetcode(result ? 0 : 1));
	}
}
