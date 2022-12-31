package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestCond;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;

@QuestValueExec(QuestExec.QUEST_EXEC_SET_QUEST_VAR)
public class ExecSetQuestVar extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        quest.getMainQuest().setQuestVar(Integer.parseInt(paramStr[0]), Integer.parseInt(paramStr[1]));
        // TODO check
        quest.getOwner().getQuestManager().queueEvent(QuestCond.QUEST_COND_QUEST_VAR_EQUAL, Integer.parseInt(paramStr[0]));
        quest.getOwner().getQuestManager().queueEvent(QuestCond.QUEST_COND_QUEST_VAR_GREATER, Integer.parseInt(paramStr[0]));
        quest.getOwner().getQuestManager().queueEvent(QuestCond.QUEST_COND_QUEST_VAR_LESS, Integer.parseInt(paramStr[0]));
        quest.getOwner().getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_QUEST_VAR_EQUAL, Integer.parseInt(paramStr[0]));
        quest.getOwner().getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_QUEST_VAR_GREATER, Integer.parseInt(paramStr[0]));
        quest.getOwner().getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_QUEST_VAR_LESS, Integer.parseInt(paramStr[0]));
        return true;
    }
}
