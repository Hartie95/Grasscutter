package emu.grasscutter.game.quest.handlers;

import emu.grasscutter.data.common.quest.SubQuestData;
import emu.grasscutter.game.quest.GameQuest;

public abstract class QuestBaseHandler<T extends SubQuestData.QuestCondition<?>> {

	public abstract boolean execute(GameQuest quest, T condition, String paramStr, int... params);

}
