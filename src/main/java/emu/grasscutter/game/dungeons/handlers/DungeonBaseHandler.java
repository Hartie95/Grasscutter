package emu.grasscutter.game.dungeons.handlers;

import org.anime_game_servers.game_data_models.gi.data.dungeon.DungeonPassData;

public abstract class DungeonBaseHandler {

	public abstract boolean execute(DungeonPassData.DungeonPassCondition condition, int... params);

}
