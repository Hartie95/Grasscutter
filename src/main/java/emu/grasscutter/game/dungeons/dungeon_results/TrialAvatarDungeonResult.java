package emu.grasscutter.game.dungeons.dungeon_results;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.game.dungeons.DungeonEndStats;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.DungeonSettleNotifyOuterClass;
import emu.grasscutter.net.proto.TrialAvatarFirstPassDungeonNotifyOuterClass.TrialAvatarFirstPassDungeonNotify;

public class TrialAvatarDungeonResult extends BaseDungeonResult {
    int trialCharacterIndexId;

    public TrialAvatarDungeonResult(DungeonData dungeonData, DungeonEndStats dungeonStats, Player player, int trialCharacterIndexId) {
        super(dungeonData, dungeonStats, player);
        this.trialCharacterIndexId = trialCharacterIndexId;
    }

    @Override
    protected void onProto(DungeonSettleNotifyOuterClass.DungeonSettleNotify.Builder builder) {
        if (this.dungeonStats.dungeonResult() == DungeonEndReason.COMPLETED) { // TODO check if it's the first pass(?)
            builder.setTrialAvatarFirstPassDungeonNotify(TrialAvatarFirstPassDungeonNotify.newBuilder()
                .setTrialAvatarIndexId(this.trialCharacterIndexId));
        }
    }
}
