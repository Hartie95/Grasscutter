package emu.grasscutter.game.dungeons;

import emu.grasscutter.game.dungeons.dungeon_results.BaseDungeonResult;
import emu.grasscutter.server.packet.send.PacketDungeonSettleNotify;
import lombok.val;

public class BasicDungeonSettleListener implements DungeonSettleListener {

    @Override
    public void onDungeonSettle(DungeonManager dungeonManager, BaseDungeonResult.DungeonEndReason endReason) {
        val scene = dungeonManager.getScene();
        val dungeonData = dungeonManager.getDungeonData();
        val time = scene.getSceneTimeSeconds() - dungeonManager.getStartSceneTime() ;
        // TODO time taken and chests handling
        DungeonEndStats stats = new DungeonEndStats(scene.getKilledMonsterCount(), time, 0, endReason);

        scene.broadcastPacket(new PacketDungeonSettleNotify(new BaseDungeonResult(dungeonData, stats)));
    }
}
