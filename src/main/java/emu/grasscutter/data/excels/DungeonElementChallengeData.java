package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@ResourceType(name = "DungeonElementChallengeExcelConfigData.json")
public class DungeonElementChallengeData extends GameResource {
    private int dungeonId;
    private List<Integer> trialAvatarId;
    private int tutorialId;

    public int getId() {
        return dungeonId;
    }
}
