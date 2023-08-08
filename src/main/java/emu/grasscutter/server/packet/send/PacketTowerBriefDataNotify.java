package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerBriefDataNotifyOuterClass.TowerBriefDataNotify;
import lombok.val;

public class PacketTowerBriefDataNotify extends BasePacket {
    public PacketTowerBriefDataNotify(Player player) {
        super(PacketOpcodes.TowerBriefDataNotify);
        val towerManager = player.getTowerManager();
        val towerScheduleManager = player.getServer().getTowerSystem();
        val monthlyBriefInfo = towerManager.getMonthlyBriefInfo();
        this.setData(TowerBriefDataNotify.newBuilder()
            .setTotalStarNum(monthlyBriefInfo.getTotalStartCount())
            .setIsFinishedEntranceFloor(towerManager.canEnterScheduleFloor())
            .setScheduleStartTime(towerScheduleManager.getScheduleStartDate())
            .setLastFloorIndex(monthlyBriefInfo.getBestFloorIndex())
            .setLastLevelIndex(monthlyBriefInfo.getBestLevelIndex())
            .setNextScheduleChangeTime(towerScheduleManager.getScheduleChangeDate())
            .setTowerScheduleId(towerScheduleManager.getCurrentTowerScheduleData().getScheduleId()));
    }
}
