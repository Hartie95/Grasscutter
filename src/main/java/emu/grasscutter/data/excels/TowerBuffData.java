package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.game.tower.TowerBuffLastingType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TowerBuffData extends GameResource {
    private int towerBuffId;
    private TowerBuffLastingType lastingType;
    private int buffId;

    @Override
    public int getId() {
        return this.towerBuffId;
    }
}
