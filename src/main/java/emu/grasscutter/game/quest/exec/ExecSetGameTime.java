package emu.grasscutter.game.quest.exec;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import lombok.val;

@QuestValueExec(QuestExec.QUEST_EXEC_SET_GAME_TIME)
public class ExecSetGameTime extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        if (paramStr.length < 1) {
            Grasscutter.getLogger().warn("no game Time specified for QUEST_EXEC_SET_GAME_TIME in quest {}", quest.getSubQuestId());
            return false;
        }
        val lockTimeArgs = paramStr[0].split("\\.");
        if (lockTimeArgs.length < 1) {
            Grasscutter.getLogger().warn("no game Time specified for QUEST_EXEC_SET_GAME_TIME in quest {}: {}", quest.getSubQuestId(), paramStr[0]);
            return false;
        }
        try {
            val hours = Integer.parseInt(lockTimeArgs[0]);
            val minutes = lockTimeArgs.length > 1 ? Integer.parseInt(lockTimeArgs[1]) : 0;
            val targetTime = hours * 60 + minutes;
            return quest.getOwner().getWorld().changeTime(targetTime, 0, true);
        } catch (NumberFormatException e) {
            Grasscutter.getLogger().warn("invalid game Time specified for QUEST_EXEC_SET_GAME_TIME in quest {}: {}", quest.getSubQuestId(), paramStr[0]);
            return false;
        }
    }
}
