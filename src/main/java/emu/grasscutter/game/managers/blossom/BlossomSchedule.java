package emu.grasscutter.game.managers.blossom;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.common.BaseBlossomROSData;
import emu.grasscutter.data.excels.BlossomChestData;
import emu.grasscutter.data.excels.BlossomGroupsData;
import emu.grasscutter.data.excels.WorldLevelData;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import emu.grasscutter.net.proto.BlossomBriefInfoOuterClass.BlossomBriefInfo;
import emu.grasscutter.net.proto.BlossomScheduleInfoOuterClass.BlossomScheduleInfo;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.utils.Position;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
@Builder(builderMethodName="of", access=AccessLevel.PRIVATE, setterPrefix="set")
@ToString
public class BlossomSchedule implements BaseBlossomROSData {
    // brief info related
    private final int sceneId;
    private final int cityId;
    private final Position position;
    private final int resin;
    private final int monsterLevel;
    private final int rewardId;
    // both info related
    private final int circleCampId;
    private final int refreshId;
    private int state;
    // schedule info related
    private int progress;
    private int round;
    private final int finishProgress;
    private final Set<Integer> remainingUid = new HashSet<>();
    // gadget info related
    private final int groupId;
    private final int decorateGroupId;
    // extra
    private final BlossomRefreshType refreshType;

    /**
     * Builder function
     * */
    public static BlossomSchedule create(@NotNull BaseBlossomROSData baseData, @NotNull BlossomGroupsData groupsData,
                                         int sceneId, int worldLevel) {

        return Optional.ofNullable(SceneGroup.of(groupsData.getNewGroupId()).load(sceneId))
            .map(group -> group.gadgets)
            .map(Map::values)
            .stream().flatMap(Collection::stream)
            .filter(gadget -> gadget.gadget_id == baseData.getRefreshType().getGadgetId())
            .map(gadget -> BlossomSchedule.of()
                .setSceneId(sceneId)
                .setCityId(baseData.getCityId())
                .setPosition(gadget.pos)
                .setResin(getResinCost(baseData.getRefreshType()))
                .setMonsterLevel(getMonsterLevel(worldLevel))
                .setRewardId(baseData.getRewardId(worldLevel))
                .setCircleCampId(groupsData.getId())
                .setRefreshId(baseData.getRefreshId())
                .setFinishProgress(groupsData.getFinishProgress())
                .setGroupId(groupsData.getNewGroupId())
                .setDecorateGroupId(groupsData.getDecorateGroupId())
                .setRefreshType(baseData.getRefreshType())
                .build())
            .findFirst().orElse(null);
    }

    /**
     * Get resin cost of current blossom camp
     * */
    public static int getResinCost(BlossomRefreshType refreshType) {
        return GameData.getBlossomChestDataMap().values().stream()
            .filter(c -> c.getBlossomRefreshType() == refreshType)
            .map(BlossomChestData::getResin)
            .findFirst().orElse(0);
    }

    /**
     * Get suitable monster level for player depending on his world level
     * */
    public static int getMonsterLevel(int worldLevel){
        return Optional.ofNullable(GameData.getWorldLevelDataMap().get(worldLevel))
            .map(WorldLevelData::getMonsterLevel)
            .orElse(26);
    }

    /**
     * Add blossom camp challenge progress (i.e. when monster die)
     * */
    public void addProgress() {
        if (getProgress() < getFinishProgress()) {
            this.progress += 1;
        }
    }

    /**
     * Check if this blossom challenge has finished
     * */
    public boolean isFinished() {
        return getProgress() >= getFinishProgress();
    }

    /**
     * Works like challenge but blossom style
     * */
    public BlossomScheduleInfo toScheduleProto() {
        return BlossomScheduleInfo.newBuilder()
            .setFinishProgress(getFinishProgress())
            .setRefreshId(getRefreshId())
            .setState(getState())
            .setRound(getRound())
            .setCircleCampId(getCircleCampId())
            .setProgress(getProgress())
            .build();
    }

    /**
     * Blossom camp information, updates the map's icon, i think...
     * */
    public BlossomBriefInfo toBriefProto() {
        return BlossomBriefInfo.newBuilder()
            .setSceneId(getSceneId())
            .setCityId(getCityId())
            .setPos(getPosition().toProto())
            .setResin(getResin())
            .setMonsterLevel(getMonsterLevel())
            .setRewardId(getRewardId())
            .setCircleCampId(getCircleCampId())
            .setRefreshId(getRefreshId())
            .setState(getState()) // 0: loaded, 1: spawned, 2:started, 3: finished
            .build();
    }

    /**
     * Morphic contract to cooperate with refresh data
     * */
    @Override
    public int getRewardId(int worldLevel) {
        return getRewardId();
    }
}
