package emu.grasscutter.game.dungeons.settle_listeners;

import emu.grasscutter.game.activity.trialavatar.TrialAvatarActivityHandler;
import emu.grasscutter.game.dungeons.DungeonEndStats;
import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.dungeon_results.BaseDungeonResult;
import emu.grasscutter.game.dungeons.dungeon_results.TrialAvatarDungeonResult;
import emu.grasscutter.server.packet.send.PacketDungeonSettleNotify;
import lombok.val;

import java.util.Optional;

import static emu.grasscutter.game.props.ActivityType.NEW_ACTIVITY_TRIAL_AVATAR;

public class TrialAvatarDungeonSettleListener implements DungeonSettleListener{
    @Override
    public void onDungeonSettle(DungeonManager dungeonManager, BaseDungeonResult.DungeonEndReason endReason) {
        val scene = dungeonManager.getScene();
        val hostPlayer = scene.getWorld().getHost();

        hostPlayer.sendPacket(new PacketDungeonSettleNotify(TrialAvatarDungeonResult.TrialAvatarBuilder()
            .setDungeonData(dungeonManager.getDungeonData())
            .setDungeonStats(new DungeonEndStats(scene.getKilledMonsterCount(), Optional.ofNullable(scene.getChallenge())
                .map(WorldChallenge::getTimeTaken).orElse(0), 0, endReason))
            .setPlayer(hostPlayer)
            .setTrialCharacterIndexId(hostPlayer.getActivityManager()
                .getActivityHandlerAs(NEW_ACTIVITY_TRIAL_AVATAR, TrialAvatarActivityHandler.class)
                .map(TrialAvatarActivityHandler::getSelectedTrialAvatarIndex).orElse(0))
            .build()));
    }
}
