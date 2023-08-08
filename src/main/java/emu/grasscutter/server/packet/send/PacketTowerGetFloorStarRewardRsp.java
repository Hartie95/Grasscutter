package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerGetFloorStarRewardRspOuterClass.TowerGetFloorStarRewardRsp;

public class PacketTowerGetFloorStarRewardRsp extends BasePacket {
    public PacketTowerGetFloorStarRewardRsp(boolean result, int floorId) {
        super(PacketOpcodes.TowerGetFloorStarRewardRsp);
        this.setData(TowerGetFloorStarRewardRsp.newBuilder()
            .setFloorId(floorId)
            .setRetcode(result ? 0 : 1));
    }
}
