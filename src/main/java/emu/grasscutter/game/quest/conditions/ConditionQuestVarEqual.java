package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_QUEST_VAR_EQUAL;

@QuestValueCond(QUEST_COND_QUEST_VAR_EQUAL)
public class ConditionQuestVarEqual extends BaseCondition {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
        var index = condition.getParam()[0];
        var questVars = quest.getMainQuest().getQuestVars();
        if (index >= questVars.length) {
            Grasscutter.getLogger().error("questVar out of bounds for {} index {} size {}", quest.getSubQuestId(), index, questVars.length);
            return false;
        }
        int questVarValue = questVars[index];
        Grasscutter.getLogger().debug("questVar {} : {}", index, questVarValue);
        return questVarValue == condition.getParam()[1];
    }
}
