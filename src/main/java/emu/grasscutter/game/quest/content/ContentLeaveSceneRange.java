package emu.grasscutter.game.quest.content;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScriptSceneData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueContent;
import emu.grasscutter.utils.Position;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_LEAVE_SCENE_RANGE;

import java.util.List;
import java.util.Map;

@QuestValueContent(QUEST_CONTENT_LEAVE_SCENE_RANGE)
public class ContentLeaveSceneRange extends BaseContent {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestContentCondition condition, String paramStr, int... params) {
        if (condition.getParam()[0] != params[0]) return false;
        ScriptSceneData fullGlobals = GameData.getScriptSceneDataMap().get("flat.luas.scenes.full_globals.lua.json");
        if (fullGlobals == null) return false;

        ScriptSceneData.ScriptObject dummyPointScript = fullGlobals.getScriptObjectList().get(params[0] + "/scene" + params[0] + "_dummy_points.lua");
        if (dummyPointScript == null) return false;

        Map<String, List<Float>> dummyPointMap = dummyPointScript.getDummyPoints();
        if (dummyPointMap == null) return false;

        List<Float> scenePosPos = dummyPointMap.get(condition.getParamStr() + ".pos");
        if (scenePosPos == null) return false;

        Position scenePos = new Position(scenePosPos.get(0), scenePosPos.get(1), scenePosPos.get(2));
        if (quest.getOwner().getPosition().computeDistance(scenePos) <= condition.getParam()[1]) return false;
        return true;
    }

}
