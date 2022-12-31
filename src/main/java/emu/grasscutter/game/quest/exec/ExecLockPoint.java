package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketScenePointUnlockNotify;

@QuestValueExec(QuestExec.QUEST_EXEC_LOCK_POINT)
public class ExecLockPoint extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        // Send packet.
        quest.getOwner().sendPacket(new PacketScenePointUnlockNotify(
            Integer.parseInt(paramStr[0]), 
            Integer.parseInt(paramStr[1]), 
            false));
        // Done.
        return true;
    }
}
