package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.game.entity.EntityMonster;
import lombok.val;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_CLEAR_GROUP_MONSTER;

@QuestValueContent(QUEST_CONTENT_CLEAR_GROUP_MONSTER)
public class ContentClearGroupMonster extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
        if (condition.getParam()[0] != params[0]) return false;
        // TODO check
        return quest.getOwner().getScene().getEntities().values().stream()
            .filter(e -> e instanceof EntityMonster && e.getGroupId() == condition.getParam()[0])
            .count() <= condition.getCount();
    }

}
