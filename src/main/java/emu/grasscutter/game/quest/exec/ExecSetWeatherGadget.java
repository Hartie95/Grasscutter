package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ClimateType;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import emu.grasscutter.server.packet.send.PacketSceneAreaWeatherNotify;

import java.util.Arrays;

@QuestValueExec(QuestExec.QUEST_EXEC_SET_WEATHER_GADGET)
public class ExecSetWeatherGadget extends QuestExecHandler {
    @Override
    public boolean execute(GameQuest quest, QuestData.QuestExecParam condition, String... paramStr) {
        // TODO check
        int param1 = Integer.parseInt(condition.getParam()[0]);
        int param2 = Integer.parseInt(condition.getParam()[1]);
        if (param2 > 0) {
            GameData.getSceneLevel2AreaMap().get(quest.getOwner().getSceneId()).stream()
                .filter(x -> quest.getOwner().getPosition().isWithinArea(
                    x.getPolygonData().getMinArea(), 
                    x.getPolygonData().getMaxArea()))
                .forEach(x -> {
                    quest.getOwner().setWorldAreaData(x);
                    quest.getOwner().getCurrentWeatherClimate().set(param1, param2);
                });
            quest.getOwner().sendPacket(new PacketSceneAreaWeatherNotify(quest.getOwner()));
            return true;
        }
        quest.getOwner().getWeatherClimateMap().get(quest.getOwner().getSceneId()).getWeatherClimate()
            .values().stream().forEach(x -> x.set(param1, param2));

        emu.grasscutter.Grasscutter.getLogger().info("Set weather to {}:{} for quest {}", param1, param2, quest.getSubQuestId());
        quest.getOwner().sendPacket(new PacketSceneAreaWeatherNotify(quest.getOwner()));
        return true;
    }
}
