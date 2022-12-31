package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.player.CityInfoItem;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

import java.util.stream.Stream;

@QuestValueContent(QuestContent.QUEST_CONTENT_OBTAIN_VARIOUS_ITEM)
public class ContentObtainVariousItem extends BaseContent {

	@Override
	public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {

		return Stream.of(condition.getParamStr().split(",")).map(Integer::parseInt)
			.allMatch(i -> quest.getOwner().getInventory().getItemByGuid(i) != null 
				&& quest.getOwner().getInventory().getItemByGuid(i).getCount() >= condition.getCount());
	}

}
