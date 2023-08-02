package emu.grasscutter.game.dungeons;

import emu.grasscutter.game.dungeons.dungeon_results.BaseDungeonResult;
import lombok.Getter;

public record DungeonEndStats(@Getter int killedMonsters, @Getter int timeTaken, @Getter int openChestCount,
                              @Getter BaseDungeonResult.DungeonEndReason dungeonResult) {
}
