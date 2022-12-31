package emu.grasscutter.game.quest.exec;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketPlayerGameTimeNotify;

import lombok.val;

@QuestValueExec(QuestExec.QUEST_EXEC_CHANGE_SKILL_DEPOT)
public class ExecChangeSkillDepot extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        val skillDepotId = Integer.parseInt(condition.getParam()[0]);
        val owner = quest.getOwner();
        val mainAvatar = owner.getAvatars().getAvatarById(owner.getMainCharacterId());

        if(mainAvatar == null){
            Grasscutter.getLogger().error("Failed to get main avatar for use {}", owner.getUid());
            return false;
        }

        Grasscutter.getLogger().info("Changing skill depot to {} for quest {}", skillDepotId, quest.getSubQuestId());
        return mainAvatar.changeSkillDepot(skillDepotId);
    }
}
