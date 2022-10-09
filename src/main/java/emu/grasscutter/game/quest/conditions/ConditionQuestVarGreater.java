package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameMainQuest;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValue;
import emu.grasscutter.game.quest.enums.QuestTrigger;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

@QuestValue(QuestTrigger.QUEST_COND_QUEST_VAR_GREATER)
public class ConditionQuestVarGreater extends QuestBaseHandler {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestCondition condition, String paramStr, int... params) {
        int questVarValue = quest.getMainQuest().getQuestVars()[condition.getParam()[0]];
        Grasscutter.getLogger().debug("questVar {} : {}", condition.getParam()[0],questVarValue);
        return questVarValue > condition.getParam()[1];
    }
}
