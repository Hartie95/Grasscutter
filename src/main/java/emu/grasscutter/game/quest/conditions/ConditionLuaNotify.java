package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_LUA_NOTIFY;

@QuestValueCond(QUEST_COND_LUA_NOTIFY)
public class ConditionLuaNotify extends BaseCondition {
    //Wrong implementation. Example: 7010226 has no paramStr
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
        return condition.getParam()[0] == Integer.parseInt(paramStr);
    }

}
