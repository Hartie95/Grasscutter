package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketPlayerWorldSceneInfoListNotify;


@QuestValueExec(QuestExec.QUEST_EXEC_LOCK_PLAYER_WORLD_SCENE)
public class ExecLockPlayerWorldScene extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        quest.getOwner().getSceneTag(Integer.parseInt(condition.getParam()[0])).lock();

        // TODO, not sure if we only have to update the changed one or the entire list
        quest.getOwner().sendPacket(new PacketPlayerWorldSceneInfoListNotify(quest.getOwner()));
        return true;
    }
}
