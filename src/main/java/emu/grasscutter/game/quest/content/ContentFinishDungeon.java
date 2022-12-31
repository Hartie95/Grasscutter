package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_FINISH_DUNGEON;

import java.util.stream.Stream;

@QuestValueContent(QUEST_CONTENT_FINISH_DUNGEON)
public class ContentFinishDungeon extends BaseContent {

    // params[0] dungeon ID, those with param [1] is all for world dungeon level up
    // it might be a problem for 2500502 and 2500702, they have the same dungeonId and param[1] 
    // for some reason
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
        // TODO check
        return quest.getOwner().getPlotDungeon(condition.getParam()[0], true) != null;
    }

}
