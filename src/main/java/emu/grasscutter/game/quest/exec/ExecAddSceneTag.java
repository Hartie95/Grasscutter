package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import lombok.val;
import java.util.HashSet;

@QuestValueExec(QuestExec.QUEST_EXEC_ADD_SCENE_TAG)
public class ExecAddSceneTag extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        val SceneNumber = Integer.parseInt(paramStr[0]);
        val SceneTagNumber = Integer.parseInt(paramStr[1]);
        val TagSet = quest.getOwner().getSceneTags().getOrDefault(SceneNumber, new HashSet<>());
        TagSet.add(SceneTagNumber);
        quest.getOwner().getSceneTags().put(SceneNumber, TagSet);
        return true;
    }
}
