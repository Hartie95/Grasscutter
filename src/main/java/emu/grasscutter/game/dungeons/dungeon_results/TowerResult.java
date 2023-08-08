package emu.grasscutter.game.dungeons.dungeon_results;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.game.dungeons.DungeonEndStats;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.DungeonSettleNotifyOuterClass;
import emu.grasscutter.net.proto.TowerLevelEndNotifyOuterClass.TowerLevelEndNotify;
import lombok.Builder;
import lombok.val;

import java.util.List;
import java.util.stream.IntStream;

import static emu.grasscutter.net.proto.TowerLevelEndNotifyOuterClass.TowerLevelEndNotify.ContinueStateType.*;

public class TowerResult extends BaseDungeonResult {
    private final boolean hasNextFloor;
    private final boolean hasNextLevel;
    private final int nextFloorId;
    private final int stars;
    private final List<GameItem> rewardItems;

    @Builder(builderMethodName = "TowerBuilder", setterPrefix = "set")
    public TowerResult(DungeonData dungeonData, DungeonEndStats dungeonStats, Player player,
                       boolean hasNextLevel, boolean hasNextFloor, int nextFloorId, int stars,
                       List<GameItem> rewardItems) {
        super(dungeonData, dungeonStats, player);
        this.hasNextFloor = hasNextFloor;
        this.hasNextLevel = hasNextLevel;
        this.nextFloorId = this.hasNextLevel ? 0 : nextFloorId;
        this.stars = stars;
        this.rewardItems = rewardItems;
    }

    @Override
    protected void onProto(DungeonSettleNotifyOuterClass.DungeonSettleNotify.Builder builder) {
        val success = this.dungeonStats.getDungeonResult().isSuccess();

        builder.setTowerLevelEndNotify(TowerLevelEndNotify.newBuilder()
            .setIsSuccess(success)
            .setContinueState(success && this.hasNextFloor ? this.hasNextLevel ?
                CONTINUE_STATE_TYPE_CAN_ENTER_NEXT_LEVEL_VALUE :
                CONTINUE_STATE_TYPE_CAN_ENTER_NEXT_FLOOR_VALUE :
                CONTINUE_STATE_TYPE_CAN_NOT_CONTINUE_VALUE)
            .addAllFinishedStarCondList(IntStream.rangeClosed(1, this.stars).boxed().toList())
            .setNextFloorId(this.hasNextFloor ? this.nextFloorId : 0)
            .addAllRewardItemList(this.rewardItems.stream().map(GameItem::toItemParam).toList()));
    }
}
