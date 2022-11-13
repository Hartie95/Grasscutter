package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_ACTIVITY_COND;

@QuestValueCond(QUEST_COND_ACTIVITY_COND)
public class ConditionActivityCond extends BaseCondition {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
        return quest.getOwner().getActivityManager().meetsCondition(condition.getParam()[0]);
    }
}
