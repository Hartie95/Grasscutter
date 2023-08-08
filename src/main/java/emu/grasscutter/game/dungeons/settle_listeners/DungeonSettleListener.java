package emu.grasscutter.game.dungeons.settle_listeners;

import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.dungeon_results.BaseDungeonResult;

public interface DungeonSettleListener {
    void onDungeonSettle(DungeonManager dungeonManager, BaseDungeonResult.DungeonEndReason endReason);
}
