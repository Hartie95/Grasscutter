package emu.grasscutter.game.quest.conditions;

import com.mongodb.lang.Nullable;
import emu.grasscutter.data.common.quest.SubQuestData;
import emu.grasscutter.data.common.quest.SubQuestData.*;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.QuestValueCond;
import lombok.val;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_ACTIVITY_COND;

@QuestValueCond(QUEST_COND_ACTIVITY_COND)
public class ConditionActivityCond extends BaseCondition {

    @Override
    public boolean execute(Player owner, SubQuestData questData, QuestAcceptCondition condition, @Nullable String paramStr, int... params) {
        val activityCondId = condition.getParam()[0];
        val targetState = condition.getParam()[1]; // only 1 for now
        return owner.getActivityManager().meetsCondition(activityCondId) == (targetState == 1);
    }

}
