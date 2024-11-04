package emu.grasscutter.game.dungeons.pass_condition;

import emu.grasscutter.game.dungeons.DungeonValue;
import emu.grasscutter.game.dungeons.handlers.DungeonBaseHandler;
import org.anime_game_servers.game_data_models.gi.data.dungeon.DungeonPassConditionType;
import org.anime_game_servers.game_data_models.gi.data.dungeon.DungeonPassData;

@DungeonValue(DungeonPassConditionType.DUNGEON_COND_KILL_GROUP_MONSTER)
public class ConditionKillGroupMonster extends DungeonBaseHandler {

    @Override
    public boolean execute(DungeonPassData.DungeonPassCondition condition, int... params) {
        return params[0] == condition.getParam().get(0);
    }
}
