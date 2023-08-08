package emu.grasscutter.game.dungeons.settle_listeners;

import emu.grasscutter.game.dungeons.DungeonEndStats;
import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.dungeon_results.BaseDungeonResult;
import emu.grasscutter.server.packet.send.PacketDungeonSettleNotify;
import lombok.val;

public class BasicDungeonSettleListener implements DungeonSettleListener {

    @Override
    public void onDungeonSettle(DungeonManager dungeonManager, BaseDungeonResult.DungeonEndReason endReason) {
        val scene = dungeonManager.getScene();
        val time = scene.getSceneTimeSeconds() - dungeonManager.getStartSceneTime();

        scene.broadcastPacket(new PacketDungeonSettleNotify(BaseDungeonResult.BaseBuilder()
            .setDungeonData(dungeonManager.getDungeonData())
            .setDungeonStats(new DungeonEndStats(
                scene.getKilledMonsterCount(), time, scene.getKillChestCount(), endReason))
            .setPlayer(scene.getWorld().getHost()) // creator of the world, should be the host then
            .build()));
    }
}
