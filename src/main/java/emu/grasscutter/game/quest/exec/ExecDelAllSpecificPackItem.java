package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;

@QuestValueExec(QuestExec.QUEST_EXEC_DEL_ALL_SPECIFIC_PACK_ITEM)
public class ExecDelAllSpecificPackItem extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        int itemId = Integer.parseInt(paramStr[0]);
        if (quest.getOwner().getInventory().getItemByGuid(itemId) == null) return false;

        return quest.getOwner().getInventory().removeItem(quest.getOwner().getInventory().getItemByGuid(itemId));
    }
}
