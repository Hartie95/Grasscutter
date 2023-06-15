package emu.grasscutter.game.quest.exec;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.ScriptArgs;
import lombok.val;

@QuestValueExec(QuestExec.QUEST_EXEC_NOTIFY_GROUP_LUA)
public class ExecNotifyGroupLua extends QuestExecHandler {

    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        val sceneId = Integer.parseInt(paramStr[0]);
        val groupId = Integer.parseInt(paramStr[1]);

        val scene = quest.getOwner().getScene();
        val scriptManager = scene.getScriptManager();

        if(scene.getId() != sceneId) {
            return false;
        }
        scene.runWhenFinished(() -> {
            val groupInstance = scriptManager.getGroupInstanceById(groupId);

            if(groupInstance==null) {
                Grasscutter.getLogger().warn("notify, no group instance for:\n group: {} \ncondition: {} \nparamStr {}", groupId, condition, paramStr);
            }

            val eventType = quest.getState() == QuestState.QUEST_STATE_FINISHED ?
                EventType.EVENT_QUEST_FINISH : EventType.EVENT_QUEST_START;
            scriptManager.callEvent(
                new ScriptArgs(groupId, eventType, quest.getSubQuestId())
                    .setEventSource(Integer.toString(quest.getSubQuestId()))
            );
        });

        return true;
    }

}
