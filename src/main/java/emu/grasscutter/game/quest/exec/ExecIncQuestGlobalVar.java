package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.enums.QuestCond;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketQuestGlobalVarNotify;

@QuestValueExec(QuestExec.QUEST_EXEC_INC_QUEST_GLOBAL_VAR)
public class ExecIncQuestGlobalVar extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        quest.getOwner().getQuestManager().incQuestGlobalVarValue(Integer.valueOf(paramStr[0]),Integer.valueOf(paramStr[1]));
        // TODO check
        quest.getOwner().getQuestManager().queueEvent(QuestCond.QUEST_COND_QUEST_GLOBAL_VAR_EQUAL, Integer.valueOf(paramStr[0]));
        quest.getOwner().getQuestManager().queueEvent(QuestCond.QUEST_COND_QUEST_GLOBAL_VAR_GREATER, Integer.valueOf(paramStr[0]));
        quest.getOwner().sendPacket(new PacketQuestGlobalVarNotify(quest.getOwner()));
        return true;
    }
}
