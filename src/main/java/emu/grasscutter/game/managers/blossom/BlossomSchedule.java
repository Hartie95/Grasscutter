package emu.grasscutter.game.managers.blossom;

import emu.grasscutter.data.excels.BlossomGroupsData;
import emu.grasscutter.data.excels.BlossomRefreshData;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import emu.grasscutter.net.proto.BlossomBriefInfoOuterClass.BlossomBriefInfo;
import emu.grasscutter.net.proto.BlossomScheduleInfoOuterClass.BlossomScheduleInfo;
import emu.grasscutter.scripts.data.SceneGadget;
import emu.grasscutter.utils.Position;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class BlossomSchedule {
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
    private Set<Integer> remainingUid = new HashSet<>();
    // gadget info related
    private final int groupId;
    // extra
    private final BlossomRefreshType refreshType;

    public BlossomSchedule(int sceneId, int cityId, Position position, int resin, int monsterLevel, int rewardId,
                           int circleCampId, int refreshId, int finishProgress,
                           int groupId, BlossomRefreshType refreshType) {
        this.sceneId = sceneId;
        this.cityId = cityId;
        this.position = position;
        this.resin = resin;
        this.monsterLevel = monsterLevel;
        this.rewardId = rewardId;

        this.circleCampId = circleCampId;
        this.refreshId = refreshId;
        this.finishProgress = finishProgress;

        this.groupId = groupId;

        this.refreshType = refreshType;
    }

    public BlossomSchedule(@NotNull BlossomRefreshData refreshData, @NotNull BlossomGroupsData groupsData, SceneGadget gadgetData,
                           int sceneId, int resin, int monsterLevel, int woldLevel, int groupId,
                           BlossomRefreshType refreshType) {
        this(sceneId, refreshData.getCityId(),
            gadgetData == null ? new Position(0f, 0f, 0f) : gadgetData.pos,
            resin, monsterLevel - refreshData.getReviseLevel(),
            refreshData.getDropVec().get(woldLevel).getPreviewReward(),
            groupsData.getId(), refreshData.getId(), groupsData.getFinishProgress(),
            groupId, refreshType);
    }

    public BlossomSchedule(@NotNull BlossomSchedule oldSchedule, @NotNull BlossomGroupsData groupsData, SceneGadget gadgetData,
                           int sceneId, int groupId) {
        this(sceneId, oldSchedule.getCityId(),
            gadgetData == null ? new Position(0f, 0f, 0f) : gadgetData.pos,
            oldSchedule.getResin(), oldSchedule.getMonsterLevel(), oldSchedule.getRewardId(),
            groupsData.getId(), oldSchedule.getRefreshId(), groupsData.getFinishProgress(),
            groupId, oldSchedule.getRefreshType());
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
            .setState(getState()) // 0: not interacted/started, 1: unknown, 2:started, 3: finished
            .build();
    }
}
