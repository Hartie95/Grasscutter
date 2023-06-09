package emu.grasscutter.game.quest.content;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.data.common.quest.SubQuestData.QuestContentCondition;
import lombok.val;

import javax.annotation.Nullable;
import java.util.Arrays;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_COMPLETE_ANY_TALK;

@QuestValueContent(QUEST_CONTENT_COMPLETE_ANY_TALK)
public class ContentCompleteAnyTalk extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestContentCondition condition, @Nullable String paramStr, int... params) {
        if(condition.getParamString() == null) {
            Grasscutter.getLogger().warn("Quest {} has no param string for QUEST_CONTENT_COMPLETE_ANY_TALK!", quest.getSubQuestId());
            return false;
        }

        val talkId = params[0];
        val conditionTalk = Arrays.stream(condition.getParamString().split(","))
            .mapToInt(Integer::parseInt)
            .toArray();
        return Arrays.stream(conditionTalk).anyMatch(tids -> tids == talkId)
            || Arrays.stream(conditionTalk).anyMatch(tids -> {
            val checkMainQuest = quest.getOwner().getQuestManager().getMainQuestByTalkId(tids);
            if (checkMainQuest == null) {
                return false;
            }
            val talkData = checkMainQuest.getTalks().get(talkId);
            return talkData != null;
        });
    }

}
