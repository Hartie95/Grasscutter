package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.player.CityInfoItem;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

@QuestValueContent(QuestContent.QUEST_CONTENT_CITY_LEVEL_UP)
public class ContentCityLevelUp extends BaseContent {

	@Override
	public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
		return condition.getParam()[0] == params[0] && condition.getParam()[1] <= params[1];
	}

}
