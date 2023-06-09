package emu.grasscutter.game.quest.content;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.data.common.quest.SubQuestData.QuestContentCondition;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_TRIGGER_FIRE;

@QuestValueContent(QUEST_CONTENT_TRIGGER_FIRE)
public class ContentTriggerFire extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestContentCondition condition, String paramStr, int... params) {
        if (quest.getTriggers().containsKey(quest.getTriggerNameById(params[0]))) {
            //We don't want to put a new key here
            return quest.getTriggers().get(quest.getTriggerNameById(params[0]));
        } else {
            Grasscutter.getLogger().error("quest {} doesn't have trigger {}", quest.getSubQuestId(), params[0]);
            return false;
        }
    }
}
