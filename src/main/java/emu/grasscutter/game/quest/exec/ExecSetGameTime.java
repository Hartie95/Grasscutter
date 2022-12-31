package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketPlayerGameTimeNotify;


@QuestValueExec(QuestExec.QUEST_EXEC_SET_GAME_TIME)
public class ExecSetGameTime extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        quest.getOwner().getScene().changeTime(Integer.parseInt(condition.getParam()[0]) * 60);
        
        quest.getOwner().sendPacket(new PacketPlayerGameTimeNotify(quest.getOwner()));
        return true;
    }
}
