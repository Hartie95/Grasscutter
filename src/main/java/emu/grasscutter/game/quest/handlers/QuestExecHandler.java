package emu.grasscutter.game.quest.handlers;

import emu.grasscutter.data.common.quest.SubQuestData.*;
import emu.grasscutter.game.quest.GameQuest;

public abstract class QuestExecHandler {

	public abstract boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr);

}
