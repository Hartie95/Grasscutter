package emu.grasscutter.game.quest.exec;

import emu.grasscutter.Grasscutter;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValue;
import emu.grasscutter.game.quest.enums.QuestTrigger;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;

@QuestValue(QuestTrigger.QUEST_EXEC_REMOVE_TRIAL_AVATAR)
public class ExecRemoveTrialAvatar extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        Grasscutter.getLogger().info("Deleted trial avatar from team for quest {}", quest.getSubQuestId());
        return quest.getOwner().removeTrialAvatar(Integer.parseInt(paramStr[0]));
    }
}
