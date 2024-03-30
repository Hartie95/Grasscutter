package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketPlayerWorldSceneInfoListNotify;
import emu.grasscutter.server.packet.send.PacketSceneDataNotify;
import lombok.val;
import java.util.HashSet;

@QuestValueExec(QuestExec.QUEST_EXEC_ADD_SCENE_TAG)
public class ExecAddSceneTag extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        val sceneNumber = Integer.parseInt(paramStr[0]);
        val sceneTagNumber = Integer.parseInt(paramStr[1]);
        val tagSet = quest.getOwner().getSceneTags().getOrDefault(sceneNumber, new HashSet<>());
        tagSet.add(sceneTagNumber);
        quest.getOwner().getSceneTags().put(sceneNumber, tagSet);
        quest.getOwner().sendPacket(new PacketSceneDataNotify(quest.getOwner()));
        quest.getOwner().sendPacket(new PacketPlayerWorldSceneInfoListNotify(quest.getOwner()));
        return true;
    }
}
