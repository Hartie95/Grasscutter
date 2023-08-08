package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@ResourceType(name = "TowerScheduleExcelConfigData.json")
@Data
@EqualsAndHashCode(callSuper = false)
public class TowerScheduleData extends GameResource {
    private int scheduleId;
    private List<Integer> entranceFloorId;
    private List<ScheduleDetail> schedules;
    private int monthlyLevelConfigId;

    @Override
    public int getId() {
        return scheduleId;
    }

    @Override
    public void onLoad() {
        this.schedules = this.schedules.stream()
            .filter(item -> !item.getFloorList().isEmpty())
            .toList();
    }

    @Data
    public static class ScheduleDetail{
        private List<Integer> floorList;
    }
}
