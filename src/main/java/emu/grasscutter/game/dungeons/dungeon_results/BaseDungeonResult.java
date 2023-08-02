package emu.grasscutter.game.dungeons.dungeon_results;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.dungeons.DungeonEndStats;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.ItemType;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.DungeonSettleNotifyOuterClass.DungeonSettleNotify;
import emu.grasscutter.net.proto.ParamListOuterClass.ParamList;
import emu.grasscutter.net.proto.StrengthenPointDataOuterClass.StrengthenPointData;
import emu.grasscutter.utils.Utils;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shows dungeon results
 * */
public class BaseDungeonResult {
    DungeonData dungeonData;
    DungeonEndStats dungeonStats;
    Player player;

    public BaseDungeonResult(DungeonData dungeonData, DungeonEndStats dungeonStats, Player player){
        this.dungeonData = dungeonData;
        this.dungeonStats = dungeonStats;
        this.player = player;
    }

    // other dungeons might not need to display this, that why I put it here
    /**
     * Could be different depending on dungeon types
     * */
    protected void onProto(DungeonSettleNotify.Builder builder){
        if (this.dungeonStats.dungeonResult().isSuccess()) return;

        val playerActiveTeam = this.player.getTeamManager().getActiveTeam();
        // show player's area to improve
        builder.putAllStrengthenPointDataMap(Arrays.stream(StrengthenPointType.values()).collect(
            Collectors.toMap(StrengthenPointType::getValue, type -> switch (type){
                case LEVEL -> StrengthenPointData.newBuilder()
                    .setBasePoint(playerActiveTeam.size() * 90)
                    .setCurPoint(playerActiveTeam.stream()
                        .map(EntityAvatar::getAvatar).map(Avatar::getLevel)
                        .reduce(0, Integer::sum))
                    .build();
                case WEAPON -> StrengthenPointData.newBuilder()
                    .setBasePoint(playerActiveTeam.size() * 90)
                    .setCurPoint(playerActiveTeam.stream()
                        .map(EntityAvatar::getAvatar).map(Avatar::getEquips)
                        .map(Map::values).flatMap(Collection::stream)
                        .filter(item -> item.getItemType() == ItemType.ITEM_WEAPON)
                        .map(GameItem::getLevel)
                        .reduce(0, Integer::sum))
                    .build();
                case TALENT -> StrengthenPointData.newBuilder()
                    .setBasePoint(100000).setCurPoint(50000).build();
                case ARTIFACT -> StrengthenPointData.newBuilder()
                    .setBasePoint(playerActiveTeam.size() * 20)
                    .setCurPoint(playerActiveTeam.stream()
                        .map(EntityAvatar::getAvatar).map(Avatar::getEquips)
                        .map(Map::values).flatMap(Collection::stream)
                        .filter(item -> item.getItemType() == ItemType.ITEM_RELIQUARY)
                        .map(GameItem::getLevel)
                        .reduce(0, Integer::sum))
                    .build();
            })));
    }

    public final DungeonSettleNotify.Builder getProto(){
        val success = this.dungeonStats.getDungeonResult().isSuccess();
        val builder = DungeonSettleNotify.newBuilder()
            .setUseTime(this.dungeonStats.getTimeTaken())
            .setDungeonId(this.dungeonData.getId())
            .setIsSuccess(success)
            .setCloseTime(getCloseTime())
            .setResult(success ? 1 : 3)
            .setCreatePlayerUid(this.player.getUid());

        // TODO check
        val tempSettleMap = new HashMap<Integer, ParamList.Builder>();
        Optional.ofNullable(this.dungeonData.getSettleShows()).stream()
            .flatMap(List::stream)
            .forEach(showType -> tempSettleMap.computeIfAbsent(showType.getId(), f -> ParamList.newBuilder())
                .addParamList(switch (showType) {
                    case SETTLE_SHOW_TIME_COST ->  this.dungeonStats.getTimeTaken();
                    case SETTLE_SHOW_KILL_MONSTER_COUNT -> this.dungeonStats.getKilledMonsters();
                    case SETTLE_SHOW_OPEN_CHEST_COUNT -> this.dungeonStats.getOpenChestCount();
                    default ->  0;
                }));

        builder.putAllSettleShow(tempSettleMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().build())));

        onProto(builder);
        return builder;
    }

    private int getCloseTime(){
        return Utils.getCurrentSeconds() + switch (this.dungeonStats.getDungeonResult()){
            case COMPLETED -> this.dungeonData.getSettleCountdownTime();
            case FAILED -> this.dungeonData.getFailSettleCountdownTime();
            case QUIT -> this.dungeonData.getQuitSettleCountdownTime();
        };
    }

    public enum DungeonEndReason{
        COMPLETED,
        FAILED,
        QUIT;

        public boolean isSuccess(){
            return this == COMPLETED;
        }
    }

    private enum StrengthenPointType {
        LEVEL(1),
        WEAPON(2),
        ARTIFACT(3),
        TALENT(4);

        private final int value;
        StrengthenPointType (int value){
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
