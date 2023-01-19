package emu.grasscutter.game.talk.exec;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScriptSceneData;
import emu.grasscutter.data.excels.TalkConfigData;
import emu.grasscutter.data.excels.TalkConfigData.TalkExecParam;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.talk.TalkExec;
import emu.grasscutter.game.talk.TalkExecHandler;
import emu.grasscutter.game.talk.TalkValueExec;
import emu.grasscutter.utils.Position;

import java.util.Map;
import java.util.List;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_ANY_MANUAL_TRANSPORT;

@TalkValueExec(TalkExec.TALK_EXEC_TRANS_SCENE_DUMMY_POINT)
public class ExecTransSceneDummyPoint extends TalkExecHandler {
    @Override
    public void execute(Player player, TalkConfigData talkData, TalkExecParam execParam) {
        // param[0] == sceneid, param[1] == position
        if (execParam.getParam().length < 2) return;

        ScriptSceneData fullGlobals = GameData.getScriptSceneDataMap().get("flat.luas.scenes.full_globals.lua.json");
        if (fullGlobals == null) return;

        ScriptSceneData.ScriptObject dummyPointScript = fullGlobals.getScriptObjectList()
            .get(execParam.getParam()[0] + "/scene" + execParam.getParam()[0] + "_dummy_points.lua");
        if (dummyPointScript == null) return;

        Map<String, List<Float>> dummyPointMap = dummyPointScript.getDummyPoints();
        if (dummyPointMap == null) return;

        List<Float> transmitPosPos = dummyPointMap.get(execParam.getParam()[1] + ".pos");
        // List<Float> transmitPosRot = dummyPointMap.get(e.getParam()[1] + ".rot"); would be useful when transportation consider rotation
        if (transmitPosPos == null || transmitPosPos.isEmpty()) return;

        player.getWorld().transferPlayerToScene(
            player, 
            Integer.parseInt(execParam.getParam()[0]), 
            new Position(transmitPosPos.get(0), transmitPosPos.get(1), transmitPosPos.get(2)));

        player.getQuestManager().queueEvent(QUEST_CONTENT_ANY_MANUAL_TRANSPORT);
    }
}
