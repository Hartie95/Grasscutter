package emu.grasscutter.game.dungeons;

import org.anime_game_servers.game_data_models.gi.data.dungeon.DungeonPassConditionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DungeonValue {
    DungeonPassConditionType value();
}
