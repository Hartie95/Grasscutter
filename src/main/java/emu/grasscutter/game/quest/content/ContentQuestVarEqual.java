package emu.grasscutter.game.quest.content;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.data.common.quest.SubQuestData.QuestContentCondition;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_QUEST_VAR_EQUAL;

@QuestValueContent(QUEST_CONTENT_QUEST_VAR_EQUAL)
public class ContentQuestVarEqual extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestContentCondition condition, String paramStr, int... params) {
        int questVarValue = quest.getMainQuest().getQuestVars()[params[0]];
        Grasscutter.getLogger().debug("questVar {} : {}", params[0], questVarValue);
        return questVarValue == params[1];
    }
}
