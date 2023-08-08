package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerBuffSelectRspOuterClass.TowerBuffSelectRsp;

public class PacketTowerBuffSelectRsp extends BasePacket {
    public PacketTowerBuffSelectRsp(int towerBuffId) {
        super(PacketOpcodes.TowerBuffSelectRsp);
        this.setData(TowerBuffSelectRsp.newBuilder()
            .setTowerBuffId(towerBuffId).build());
    }
}
