package emu.grasscutter.game.quest.content;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.data.common.quest.SubQuestData.QuestContentCondition;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

@QuestValueContent(QuestContent.QUEST_CONTENT_NONE)
public class BaseContent extends QuestBaseHandler<QuestContentCondition> {

	@Override
	public boolean execute(GameQuest quest, QuestContentCondition condition, String paramStr, int... params) {
		// TODO Auto-generated method stub
        Grasscutter.getLogger().error("Unknown condition {} at {}", condition.getType().name(), quest.getSubQuestId());
        return false;
	}

}
