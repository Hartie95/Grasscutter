package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketMapAreaChangeNotify;

import java.util.Arrays;

@QuestValueExec(QuestExec.QUEST_EXEC_CHANGE_MAP_AREA_STATE)
public class ExecChangeMapAreaState extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        quest.getOwner().getMapAreaInfoMap()
            .get(Integer.parseInt(condition.getParam()[0]))
            .changeState(Integer.parseInt(condition.getParam()[1]));
        // not sure if should update the changed one only or the entire map
        quest.getOwner().sendPacket(new PacketMapAreaChangeNotify(quest.getOwner()));
        return true;
    }
}
