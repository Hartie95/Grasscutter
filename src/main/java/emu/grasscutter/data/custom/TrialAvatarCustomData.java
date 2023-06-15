package emu.grasscutter.data.custom;

import emu.grasscutter.data.common.BaseTrialAvatarData;
import emu.grasscutter.data.common.BaseTrialAvatarTemplateData;
import lombok.*;
import java.util.List;

@Data
public class TrialAvatarCustomData implements BaseTrialAvatarData, BaseTrialAvatarTemplateData {
    private int trialAvatarId;
    private List<Integer> trialAvatarParamList;
    private List<Integer> trialAvatarWeaponList;
    private List<Integer> trialReliquaryList;
    private int trialSkillDepotId;
    private int trialAvatarSkillLevel;
}
