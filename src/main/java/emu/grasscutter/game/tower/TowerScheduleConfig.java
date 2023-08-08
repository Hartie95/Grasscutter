package emu.grasscutter.game.tower;

import lombok.Data;

import java.util.Date;

@Data
public class TowerScheduleConfig {
    private int scheduleId;
    private Date scheduleStartTime;
    private Date nextScheduleChangeTime;
}
