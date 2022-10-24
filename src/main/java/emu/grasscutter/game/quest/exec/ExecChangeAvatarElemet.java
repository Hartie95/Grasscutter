package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.managers.skill.SkillManager;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValue;
import emu.grasscutter.game.quest.enums.QuestTrigger;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.Grasscutter;

@QuestValue(QuestTrigger.QUEST_EXEC_CHANGE_AVATAR_ELEMET)
public class ExecChangeAvatarElemet extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        var paramInt = Integer.parseInt(paramStr[0]);
        Grasscutter.getLogger().info("Changing skill element for quest {}", quest.getSubQuestId());
        return SkillManager.changeAvatarElement(quest.getOwner(), paramInt);
    }
}
