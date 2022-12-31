package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.player.CityInfoItem;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.handlers.QuestBaseHandler;

@QuestValueContent(QuestContent.QUEST_CONTENT_TEAM_DEAD)
public class ContentTeamDead extends BaseContent {

	@Override
	public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
		return quest.getOwner().getTeamManager().getActiveTeam().stream().allMatch(e -> !e.isAlive());
	}

}
