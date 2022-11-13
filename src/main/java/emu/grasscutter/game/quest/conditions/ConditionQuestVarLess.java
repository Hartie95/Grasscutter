package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_QUEST_VAR_LESS;

@QuestValueCond(QUEST_COND_QUEST_VAR_LESS)
public class ConditionQuestVarLess extends BaseCondition {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
        int questVarValue = quest.getMainQuest().getQuestVars()[condition.getParam()[0]];
        Grasscutter.getLogger().debug("questVar {} : {}", condition.getParam()[0], questVarValue);
        return questVarValue < condition.getParam()[1];
    }
}
