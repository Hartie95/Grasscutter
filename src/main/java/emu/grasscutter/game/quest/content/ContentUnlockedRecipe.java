package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

import java.util.Arrays;

@QuestValueContent(QuestContent.QUEST_CONTENT_UNLOCKED_RECIPE)
public class ContentUnlockedRecipe extends BaseContent {

	@Override
	public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
		return Arrays.stream(paramStr.split(",")).map(Integer::parseInt)
			.allMatch(x -> x == params[0]);
	}

}
