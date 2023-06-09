package emu.grasscutter.game.quest.content;

import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.data.common.quest.SubQuestData.QuestContentCondition;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_PLAYER_LEVEL_UP;

@QuestValueContent(QUEST_CONTENT_PLAYER_LEVEL_UP)
public class ContentPlayerLevelUp extends BaseContent {
    @Override
    public boolean execute(GameQuest quest, QuestContentCondition condition, String paramStr, int... params) {
        return quest.getOwner().getLevel() >= condition.getCount();
    }
}
