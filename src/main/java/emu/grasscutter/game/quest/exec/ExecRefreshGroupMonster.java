package emu.grasscutter.game.quest.exec;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketGroupSuiteNotify;


@QuestValueExec(QuestExec.QUEST_EXEC_REFRESH_GROUP_MONSTER)
public class ExecRefreshGroupMonster extends QuestExecHandler {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        int groupId = Integer.parseInt(paramStr[0]);
        var scriptManager = quest.getOwner().getScene().getScriptManager();
        // TODO check
        if (scriptManager.getGroupById(groupId) == null) {
            Grasscutter.getLogger().warn("trying to load unknown group monster {}", groupId);
        } else {
            scriptManager.refreshGroup(scriptManager.getGroupById(groupId));
            quest.getOwner().sendPacket(new PacketGroupSuiteNotify(groupId, 0)); // maybe its 1?
        }
        
        return true;
    }

}
