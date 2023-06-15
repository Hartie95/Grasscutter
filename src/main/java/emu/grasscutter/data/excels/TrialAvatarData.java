package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.data.common.BaseTrialAvatarData;
import lombok.*;
import java.util.List;

@ResourceType(name = "TrialAvatarExcelConfigData.json")
@EqualsAndHashCode(callSuper=false)
@Data
public class TrialAvatarData extends GameResource implements BaseTrialAvatarData {
    private int trialAvatarId;
    private List<Integer> trialAvatarParamList;
    private int trialSkillDepotId;

    @Override
    public int getId() {
        return trialAvatarId;
    }

    @Override
    public List<Integer> getTrialAvatarWeaponList() {
        return List.of();
    }
}
