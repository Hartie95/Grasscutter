package emu.grasscutter.game.quest.content;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.data.common.quest.SubQuestData.QuestContentCondition;
import emu.grasscutter.game.quest.enums.QuestContent;
import lombok.val;

@QuestValueContent(QuestContent.QUEST_CONTENT_TIME_VAR_GT_EQ)
public class ContentTimeVarMoreOrEqual extends BaseContent{
    @Override
    public boolean execute(GameQuest quest, QuestContentCondition condition, String paramStr, int... params) {
        if (condition.getParamString() == null) {
            Grasscutter.getLogger().warn("Quest {} has no param string for QUEST_CONTENT_TIME_VAR_GT_EQ!", quest.getSubQuestId());
            return false;
        }
        val mainQuestId = condition.getParam()[0];
        val timeVarIndex = condition.getParam()[1];
        val minTime = Integer.parseInt(condition.getParamString());

        val mainQuest = quest.getOwner().getQuestManager().getMainQuestById(mainQuestId);

        if(mainQuest == null){
            return false;
        }

        return mainQuest.getHoursSinceTimeVar(timeVarIndex) >= minTime;
    }
}
