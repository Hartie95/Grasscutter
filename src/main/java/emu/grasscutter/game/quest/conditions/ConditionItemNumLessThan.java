package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.data.common.quest.SubQuestData;
import emu.grasscutter.data.common.quest.SubQuestData.*;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.QuestValueCond;
import lombok.val;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_ITEM_NUM_LESS_THAN;

@QuestValueCond(QUEST_COND_ITEM_NUM_LESS_THAN)
public class ConditionItemNumLessThan extends BaseCondition {

    @Override
    public boolean execute(Player owner, SubQuestData questData, QuestAcceptCondition condition, String paramStr, int... params) {
        val itemId = condition.getParam()[0];
        val amount = condition.getParam()[1];
        val checkItem = owner.getInventory().getItemByGuid(itemId);
        return checkItem == null || checkItem.getCount() < amount;
    }

}
