package emu.grasscutter.game.dungeons.dungeon_results;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.game.dungeons.DungeonEndStats;
import emu.grasscutter.net.proto.DungeonSettleNotifyOuterClass.DungeonSettleNotify;
import emu.grasscutter.net.proto.ParamListOuterClass.ParamList;
import emu.grasscutter.utils.Utils;
import lombok.Getter;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class BaseDungeonResult {
    @Getter DungeonData dungeonData;
    @Getter
    DungeonEndStats dungeonStats;

    public BaseDungeonResult(DungeonData dungeonData, DungeonEndStats dungeonStats){
        this.dungeonData = dungeonData;
        this.dungeonStats = dungeonStats;
    }

    protected void onProto(DungeonSettleNotify.Builder builder){ }

    public final DungeonSettleNotify.Builder getProto(){
        var builder = DungeonSettleNotify.newBuilder()
            .setDungeonId(dungeonData.getId())
            .setIsSuccess(dungeonStats.getDungeonResult().isSuccess())
            .setCloseTime(getCloseTime())
            .setResult(dungeonStats.getDungeonResult().getValue());

        // TODO check
        if(dungeonData.getSettleShows()!=null) {
            Map<Integer, ParamList.Builder> settleShowHolder = new HashMap<>();
            dungeonData.getSettleShows().stream().forEach(x -> {
                settleShowHolder.computeIfAbsent(x.getId(), s -> ParamList.newBuilder())
                    .addParamList(switch (x) {
                            case SETTLE_SHOW_TIME_COST -> dungeonStats.getTimeTaken();
                            case SETTLE_SHOW_KILL_MONSTER_COUNT -> dungeonStats.getKilledMonsters();
                            default -> 0;
                        }
                    );
            });

            builder.putAllSettleShow(settleShowHolder.entrySet().stream()
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue().build())));
        }

        //TODO handle settle show

        // onProto(builder);

        return builder;
    }

    public int getCloseTime(){
        return Utils.getCurrentSeconds() + switch (dungeonStats.getDungeonResult()){
            case COMPLETED -> dungeonData.getSettleCountdownTime();
            case FAILED -> dungeonData.getFailSettleCountdownTime();
            case QUIT -> dungeonData.getQuitSettleCountdownTime();
        };
    }

    public enum DungeonEndReason{
        COMPLETED(1),
        FAILED(2),
        QUIT(3);

        private final int value;

        DungeonEndReason(int value) {
            this.value = value;
        }

        public boolean isSuccess(){
            return this == COMPLETED;
        }

        public int getValue() {
            return value;
        }
    }
}
