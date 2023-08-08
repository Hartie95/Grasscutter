package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.tower.TowerManager;
import emu.grasscutter.game.tower.TowerSystem;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerAllDataRspOuterClass.TowerAllDataRsp;

import java.util.stream.Collectors;

public class PacketTowerAllDataRsp extends BasePacket {

    public PacketTowerAllDataRsp(TowerSystem towerSystem, TowerManager towerManager) {
        super(PacketOpcodes.TowerAllDataRsp);

        this.setData(TowerAllDataRsp.newBuilder()
            .setTowerScheduleId(towerSystem.getCurrentTowerScheduleData().getScheduleId())
            .addAllTowerFloorRecordList(towerManager.getTowerData().getTowerRecordProtoList())
            .setCurLevelRecord(towerManager.getCurRecordProto())
            .setScheduleStartTime(towerSystem.getScheduleStartDate())
            .setNextScheduleChangeTime(towerSystem.getScheduleChangeDate())
            .putAllFloorOpenTimeMap(towerSystem.getScheduleFloors().stream()
                .collect(Collectors.toMap(x -> x, y -> towerSystem.getScheduleStartDate())))
            .setIsFinishedEntranceFloor(towerManager.canEnterScheduleFloor())
            .setLastScheduleMonthlyBrief(towerManager.getMonthlyBriefInfo().toProto())
            .setMonthlyBrief(towerManager.getMonthlyBriefInfo().toProto())
//            .setIsFirstInteract(towerManager.getTowerData().getRecordMap().isEmpty())
            .build());
    }
}
