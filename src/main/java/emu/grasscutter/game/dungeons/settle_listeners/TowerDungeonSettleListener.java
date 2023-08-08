package emu.grasscutter.game.dungeons.settle_listeners;

import emu.grasscutter.game.dungeons.DungeonEndStats;
import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.dungeon_results.BaseDungeonResult.DungeonEndReason;
import emu.grasscutter.game.dungeons.dungeon_results.TowerResult;
import emu.grasscutter.server.packet.send.PacketDungeonSettleNotify;
import lombok.val;

import java.util.Optional;

public class TowerDungeonSettleListener implements DungeonSettleListener {

    @Override
    public void onDungeonSettle(DungeonManager dungeonManager, DungeonEndReason endReason) {
        val scene = dungeonManager.getScene();
        val hostPlayer = scene.getWorld().getHost();
        val towerManager = hostPlayer.getTowerManager();
        val challenge = scene.getChallenge();

        hostPlayer.sendPacket(new PacketDungeonSettleNotify(TowerResult.TowerBuilder()
            .setDungeonData(dungeonManager.getDungeonData())
            .setDungeonStats(new DungeonEndStats(scene.getKilledMonsterCount(), Optional.ofNullable(challenge)
                .map(WorldChallenge::getTimeTaken).orElse(0), 0, endReason))
            .setPlayer(hostPlayer)
            .setHasNextFloor(towerManager.hasNextFloor())
            .setHasNextLevel(towerManager.hasNextLevel())
            .setNextFloorId(towerManager.getNextFloorId())
            .setRewardItems(towerManager.giveFirstPassReward())
            // calculate star should always be the last one, as it increments the floor and level index
            .setStars(towerManager.calculateStar(challenge))
            .build()));
    }
}
