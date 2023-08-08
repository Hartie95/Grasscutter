package emu.grasscutter.game.tower;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.DataLoader;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.TowerScheduleData;
import emu.grasscutter.server.game.BaseGameSystem;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.utils.DateHelper;
import lombok.Getter;
import lombok.val;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TowerSystem extends BaseGameSystem {
    @Getter private final static TowerScheduleConfig towerScheduleConfig;

    public TowerSystem(GameServer server) {
        super(server);
    }

    static {
        TowerScheduleConfig temp = null;
        try {
            temp = DataLoader.loadClass("TowerSchedule.json", TowerScheduleConfig.class);
        } catch (Exception e) {
            Grasscutter.getLogger().error("Unable to load tower schedule config.", e);
        }
        towerScheduleConfig = temp;
    }

    public TowerScheduleData getCurrentTowerScheduleData() {
        val data = GameData.getTowerScheduleDataMap().get(getScheduleId());
        if (data == null) {
            Grasscutter.getLogger().error(
                "Could not get current tower schedule data by schedule id {}, please check your resource files", getScheduleId());
        }

        return data;
    }

    public List<Integer> getEntranceFloor() {
        return Optional.ofNullable(getCurrentTowerScheduleData()).map(TowerScheduleData::getEntranceFloorId)
            .stream().flatMap(List::stream).toList();
    }

    public List<Integer> getScheduleFloors() {
        return Optional.ofNullable(getCurrentTowerScheduleData()).map(TowerScheduleData::getSchedules)
            .stream().flatMap(List::stream).map(TowerScheduleData.ScheduleDetail::getFloorList).flatMap(List::stream)
            .toList();
    }

    public List<Integer> getAllFloors() {
        return Stream.of(getEntranceFloor(), getScheduleFloors()).flatMap(Collection::stream).toList();
    }

    public int getNextFloorId(int floorId) {
        val allFloors = getAllFloors();
        return IntStream.range(0, allFloors.size() - 1)
            .filter(i -> floorId == allFloors.get(i))
            .mapToObj(i -> allFloors.get(i + 1))
            .findFirst().orElse(0);
    }

    public int getLastEntranceFloor() {
        return getEntranceFloor().stream().reduce((first, second) -> second).orElse(0);
    }

    public int getScheduleId() {
        return Optional.ofNullable(towerScheduleConfig).map(TowerScheduleConfig::getScheduleId).orElse(0);
    }

    public int getScheduleStartDate() {
        return Optional.ofNullable(towerScheduleConfig).map(TowerScheduleConfig::getScheduleStartTime)
            .map(DateHelper::getUnixTime).orElse(0);
    }

    public int getScheduleChangeDate() {
        return Optional.ofNullable(towerScheduleConfig).map(TowerScheduleConfig::getNextScheduleChangeTime)
            .map(DateHelper::getUnixTime).orElse(0);
    }
}
