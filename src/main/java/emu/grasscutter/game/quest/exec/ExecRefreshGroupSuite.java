package emu.grasscutter.game.quest.exec;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestGroupSuite;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketGroupSuiteNotify;


@QuestValueExec(QuestExec.QUEST_EXEC_REFRESH_GROUP_SUITE)
public class ExecRefreshGroupSuite extends QuestExecHandler {

    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        var sceneId = Integer.parseInt(paramStr[0]);
        var entries = paramStr[1].split(";");
        for(var entry : entries) {
            var groupId = Integer.parseInt(entry.split(",")[0]);
            var suiteId = Integer.parseInt(entry.split(",")[1]);

            var scriptManager = quest.getOwner().getScene().getScriptManager();

            // mainly trying to avoid unlimited incrementation group suites that get saved to DB
            // but the following implementation sometimes throws error, and cause rewind to fail
            // in game and require login and out to work
            if (!quest.getMainQuest().getGroupSuitesTracker().contains(quest.getSubQuestId())) {
                quest.getMainQuest().getGroupSuitesTracker().add(quest.getSubQuestId());
                quest.getMainQuest().getQuestGroupSuites().add(QuestGroupSuite.of()
                .scene(sceneId)
                .group(groupId)
                .suite(suiteId)
                .build());
            }

            // refresh immediately if player is in this scene
            if (quest.getOwner().getScene().getId() == sceneId) {
                var targetGroup = scriptManager.getGroupById(groupId);
                if (targetGroup == null) {
                    Grasscutter.getLogger().warn("trying to load unknown group {} in scene {}", groupId, sceneId);
                } else {
                    scriptManager.refreshGroup(targetGroup, suiteId);
                    quest.getOwner().sendPacket(new PacketGroupSuiteNotify(groupId, suiteId));
                }
            }
        }

        return true;
    }

}
