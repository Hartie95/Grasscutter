package emu.grasscutter.game.quest.conditions;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueCond;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_NONE;

@QuestValueCond(QUEST_COND_NONE)
public class BaseCondition extends QuestBaseHandler<QuestData.QuestAcceptCondition> {

	@Override
	public boolean execute(GameQuest quest, QuestData.QuestAcceptCondition condition, String paramStr, int... params) {
		// TODO Auto-generated method stub
		return false;
	}

}
