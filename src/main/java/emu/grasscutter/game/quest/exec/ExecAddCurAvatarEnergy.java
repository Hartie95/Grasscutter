package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValue;
import emu.grasscutter.game.quest.enums.QuestTrigger;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.net.proto.PropChangeReasonOuterClass.PropChangeReason;
import emu.grasscutter.Grasscutter;

@QuestValue(QuestTrigger.QUEST_EXEC_ADD_CUR_AVATAR_ENERGY)
public class ExecAddCurAvatarEnergy extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        quest.getOwner().getEnergyManager().refillTeamEnergy(PropChangeReason.PROP_CHANGE_REASON_FINISH_QUEST);
        Grasscutter.getLogger().info("Energy refilled");
        return true;
    }
}
