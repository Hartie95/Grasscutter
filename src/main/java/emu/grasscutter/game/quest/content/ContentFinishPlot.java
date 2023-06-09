package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.common.quest.MainQuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.data.common.quest.SubQuestData.QuestContentCondition;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_FINISH_PLOT;

@QuestValueContent(QUEST_CONTENT_FINISH_PLOT)
public class ContentFinishPlot extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestContentCondition condition, String paramStr, int... params) {
        if(params.length == 0) {
            return false;
        }
        MainQuestData.TalkData talkData = quest.getMainQuest().getTalks().get(params[0]);
        GameQuest subQuest = quest.getMainQuest().getChildQuestById(params[0]);
        return talkData != null && subQuest != null || condition.getParam()[0] == params[0];
    }

}
