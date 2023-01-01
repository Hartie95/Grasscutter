package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_LUA_NOTIFY;

@QuestValueCond(QUEST_COND_LUA_NOTIFY)
public class ConditionLuaNotify extends BaseCondition {
    @Override
    public boolean execute(Player owner, QuestData questData, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
        return condition.getParam()[0] == Integer.parseInt(paramStr);
    }

}
