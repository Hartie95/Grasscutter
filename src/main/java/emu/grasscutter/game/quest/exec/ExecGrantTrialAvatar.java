package emu.grasscutter.game.quest.exec;

import emu.grasscutter.Grasscutter;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValue;
import emu.grasscutter.game.quest.enums.QuestTrigger;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;

@QuestValue(QuestTrigger.QUEST_EXEC_GRANT_TRIAL_AVATAR)
public class ExecGrantTrialAvatar extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        Grasscutter.getLogger().info("Added trial avatar to team for quest {}", quest.getSubQuestId());
        return quest.getOwner().addTrialAvatar(Integer.parseInt(paramStr[0]), quest.getMainQuestId());
    }
}
