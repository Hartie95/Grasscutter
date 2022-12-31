package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketClientLockGameTimeNotify;

@QuestValueExec(QuestExec.QUEST_EXEC_SET_IS_GAME_TIME_LOCKED)
public class ExecSetIsGameTimeLocked extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        // TODO, check
        quest.getOwner().sendPacket(new PacketClientLockGameTimeNotify(Integer.parseInt(condition.getParam()[0])));
        return true;
    }
}
