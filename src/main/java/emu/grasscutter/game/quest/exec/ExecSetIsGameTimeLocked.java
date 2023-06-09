package emu.grasscutter.game.quest.exec;

import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import lombok.val;

@QuestValueExec(QuestExec.QUEST_EXEC_SET_IS_GAME_TIME_LOCKED)
public class ExecSetIsGameTimeLocked extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        val lockTime = Integer.parseInt(paramStr[0]);
        return quest.getOwner().getWorld().setGameTimeLocked(lockTime == 1);
    }
}
