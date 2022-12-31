package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

import java.util.stream.Stream;

@QuestValueContent(QuestContent.QUEST_CONTENT_ENTER_MY_HOME_WORLD)
public class ContentEnterMyHomeWorld extends BaseContent {

	@Override
	public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
		return Stream.of(paramStr.split(",")).map(Integer::parseInt).anyMatch(e -> e == params[0]);
	}

}
