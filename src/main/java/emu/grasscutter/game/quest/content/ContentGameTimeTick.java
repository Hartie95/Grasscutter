package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_GAME_TIME_TICK;

@QuestValueContent(QUEST_CONTENT_GAME_TIME_TICK)
public class ContentGameTimeTick extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
        var range = condition.getParamStr().split(",");
        var min = Math.min(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
        var max = Math.max(Integer.parseInt(range[0]), Integer.parseInt(range[1]));

        // params[0] is clock, params[1] is day
        return params[0] >= min && params[0] <= max &&
            params[1] >= condition.getParam()[0];
    }

}
