package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketPlayerWorldSceneInfoListNotify;
import lombok.val;
import java.util.HashSet;

@QuestValueExec(QuestExec.QUEST_EXEC_DEL_SCENE_TAG)
public class ExecDelSceneTag extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        val SceneNumber = Integer.parseInt(paramStr[0]);
        val SceneTagNumber = Integer.parseInt(paramStr[1]);
        val TagSet = quest.getOwner().getSceneTags().getOrDefault(SceneNumber, new HashSet<>());
        TagSet.remove(SceneTagNumber);
        quest.getOwner().getSceneTags().put(SceneNumber, TagSet);
        quest.getOwner().sendPacket(new PacketPlayerWorldSceneInfoListNotify(quest.getOwner()));
        return true;
    }
}
