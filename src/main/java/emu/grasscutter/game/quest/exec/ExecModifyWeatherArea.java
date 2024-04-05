package emu.grasscutter.game.quest.exec;

import emu.grasscutter.data.common.quest.SubQuestData.QuestExecParam;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.QuestValueExec;
import emu.grasscutter.game.quest.enums.QuestExec;
import emu.grasscutter.game.quest.handlers.QuestExecHandler;
import lombok.val;

@QuestValueExec(QuestExec.QUEST_EXEC_MODIFY_WEATHER_AREA)
public class ExecModifyWeatherArea extends QuestExecHandler {

    @Override
    public boolean execute(GameQuest quest, QuestExecParam condition, String... paramStr) {
        var weatherAreaIds = paramStr[0].split(";");
        var openWeathers = paramStr[1].split(";");

        if(weatherAreaIds.length != openWeathers.length) return false;

        boolean success = true;
        for(int i = 0; i < weatherAreaIds.length; i++) {
            val weatherAreaId = Integer.parseInt(weatherAreaIds[i]);
            val openWeather = Integer.parseInt(openWeathers[i]) == 1 ? true : false;

            if(openWeather) {
                success = quest.getOwner().getScene().addWeatherArea(weatherAreaId);
            } else {
                success = quest.getOwner().getScene().removeWeatherArea(weatherAreaId);
            }
        }

        return success;
    }

}
