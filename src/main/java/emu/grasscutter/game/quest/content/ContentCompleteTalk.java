package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import lombok.val;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_COMPLETE_TALK;

@QuestValueContent(QUEST_CONTENT_COMPLETE_TALK)
public class ContentCompleteTalk extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
        return condition.getParam()[0] == params[0] && GameData.getTalkConfigDataMap().get(condition.getParam()[0]) != null;
    }
}
