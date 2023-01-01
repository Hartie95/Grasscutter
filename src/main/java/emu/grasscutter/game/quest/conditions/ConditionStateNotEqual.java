package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_STATE_NOT_EQUAL;

@QuestValueCond(QUEST_COND_STATE_NOT_EQUAL)
public class ConditionStateNotEqual extends BaseCondition {

    @Override
    public boolean execute(Player owner, QuestData questData, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
        GameQuest checkQuest = owner.getQuestManager().getQuestById(condition.getParam()[0]);
        if (checkQuest == null) {
            /*
            Will spam the console
            //Grasscutter.getLogger().debug("Warning: quest {} hasn't been started yet!", condition.getParam()[0]);

            */
            return false;
        }
        return checkQuest.getState().getValue() != condition.getParam()[1];
    }

}
