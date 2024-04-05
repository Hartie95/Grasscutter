package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import lombok.val;

@QuestValueExec(QuestExec.QUEST_EXEC_DEL_SCENE_TAG)
public class ExecDelSceneTag extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        val sceneNumber = Integer.parseInt(paramStr[0]);
        val sceneTagNumber = Integer.parseInt(paramStr[1]);
        quest.getOwner().setSceneTag(sceneNumber, sceneTagNumber, false);
        return true;
    }
}
