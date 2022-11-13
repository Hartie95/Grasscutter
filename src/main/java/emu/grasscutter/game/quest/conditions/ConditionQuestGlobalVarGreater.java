package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_QUEST_GLOBAL_VAR_GREATER;

@QuestValueCond(QUEST_COND_QUEST_GLOBAL_VAR_GREATER)
public class ConditionQuestGlobalVarGreater extends BaseCondition {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
        Integer questGlobalVarValue = quest.getMainQuest().getQuestManager().getQuestGlobalVarValue(condition.getParam()[0]);
        Grasscutter.getLogger().debug("questGlobarVar {} : {}", condition.getParam()[0], questGlobalVarValue);
        return questGlobalVarValue > condition.getParam()[1];
    }
}
