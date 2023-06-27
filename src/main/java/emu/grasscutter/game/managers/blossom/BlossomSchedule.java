package emu.grasscutter.game.managers.blossom;

import emu.grasscutter.data.binout.ScriptSceneData.ScriptObject.SceneGadgetData;
import emu.grasscutter.data.excels.BlossomGroupsData;
import emu.grasscutter.data.excels.BlossomRefreshData;
import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;
import emu.grasscutter.net.proto.BlossomBriefInfoOuterClass.BlossomBriefInfo;
import emu.grasscutter.net.proto.BlossomScheduleInfoOuterClass.BlossomScheduleInfo;
import emu.grasscutter.utils.Position;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
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
    private final boolean isGuideOpened = false;
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
    private final int gadgetId;
    private final int groupId;
    private final int configId;
    private final Position rotation;
    // extra
    private final BlossomRefreshType refreshType;

    public BlossomSchedule(int sceneId, int cityId, List<Float> position, int resin, int monsterLevel, int rewardId,
                           int circleCampId, int refreshId, int finishProgress,
                           int gadgetId, int groupId, int configId, List<Float> rotation,
                           BlossomRefreshType refreshType) {
        this.sceneId = sceneId;
        this.cityId = cityId;
        this.position = new Position(position);
        this.resin = resin;
        this.monsterLevel = monsterLevel;
        this.rewardId = rewardId;

        this.circleCampId = circleCampId;
        this.refreshId = refreshId;
        this.finishProgress = finishProgress;

        this.gadgetId = gadgetId;
        this.groupId = groupId;
        this.configId = configId;
        this.rotation = new Position(rotation);

        this.refreshType = refreshType;
    }

    public BlossomSchedule(BlossomRefreshData refreshData, BlossomGroupsData groupsData, SceneGadgetData gadgetData,
                           int sceneId, int resin, int monsterLevel, int woldLevel, int groupId,
                           BlossomRefreshType refreshType) {
        this(sceneId, refreshData.getCityId(),
            gadgetData == null ? List.of(0f, 0f, 0f) : gadgetData.getPos(),
            resin, monsterLevel - refreshData.getReviseLevel(),
            refreshData.getDropVec().get(woldLevel).getPreviewReward(),
            groupsData.getId(), refreshData.getId(), groupsData.getFinishProgress(),
            refreshType.getGadgetId(), groupId, gadgetData == null ? 0 : gadgetData.getConfigId(),
            gadgetData == null ? List.of(0f, 0f, 0f) : gadgetData.getRot(),
            refreshType);
    }

    public BlossomSchedule(BlossomSchedule oldSchedule, BlossomGroupsData groupsData, SceneGadgetData gadgetData,
                           int sceneId, int groupId) {
        this(sceneId, oldSchedule.getCityId(),
            gadgetData == null ? List.of(0f, 0f, 0f) : gadgetData.getPos(),
            oldSchedule.getResin(), oldSchedule.getMonsterLevel(), oldSchedule.getRewardId(),
            groupsData.getId(), oldSchedule.getRefreshId(), groupsData.getFinishProgress(),
            oldSchedule.getGadgetId(), groupId,
            gadgetData == null ? 0 : gadgetData.getConfigId(),
            gadgetData == null ? List.of(0f, 0f, 0f) : gadgetData.getRot(),
            oldSchedule.getRefreshType());
    }

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
