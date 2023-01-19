package emu.grasscutter.game.talk;

import emu.grasscutter.data.excels.TalkConfigData;
import emu.grasscutter.data.excels.TalkConfigData.TalkExecParam;
import emu.grasscutter.game.player.Player;

public abstract class TalkExecHandler {

	public abstract void execute(Player player, TalkConfigData talkData, TalkExecParam execParam);

}
