package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

import java.util.Arrays;

@QuestValueContent(QuestContent.QUEST_CONTENT_NOT_UNLOCKED_RECIPE)
public class ContentNotUnlockedRecipe extends BaseContent {

	@Override
	public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
		return !quest.getOwner().getUnlockedRecipies().containsKey(Integer.parseInt(paramStr));
	}

}
