package emu.grasscutter.game.quest.conditions;

import com.mongodb.lang.Nullable;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.common.quest.SubQuestData;
import emu.grasscutter.data.common.quest.SubQuestData.*;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.QuestValueCond;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_UNKNOWN;

@QuestValueCond(QUEST_COND_UNKNOWN)
public class BaseCondition {

    public boolean execute(Player owner, SubQuestData questData, QuestAcceptCondition condition, @Nullable String paramStr, int... params) {
        Grasscutter.getLogger().error("Unknown condition {} at {}", condition.getType().name(), questData.getSubId());
        return false;
    }

}
