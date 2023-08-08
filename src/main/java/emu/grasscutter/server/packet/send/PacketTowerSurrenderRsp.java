package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerSurrenderRspOuterClass.TowerSurrenderRsp;

public class PacketTowerSurrenderRsp extends BasePacket {
    public PacketTowerSurrenderRsp() {
        super(PacketOpcodes.TowerSurrenderRsp);
        this.setData(TowerSurrenderRsp.newBuilder());
    }
}
