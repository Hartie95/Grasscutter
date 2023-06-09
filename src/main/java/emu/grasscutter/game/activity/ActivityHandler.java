package emu.grasscutter.game.activity;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.ActivityData;
import emu.grasscutter.data.server.ActivityCondGroup;
import emu.grasscutter.game.activity.condition.ActivityConditionExecutor;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.WatcherTriggerType;
import emu.grasscutter.game.quest.enums.QuestCond;
import emu.grasscutter.net.proto.ActivityInfoOuterClass;
import emu.grasscutter.utils.DateHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class ActivityHandler {
    /**
     * Must set before initWatchers
     */
    @Getter ActivityConfigItem activityConfigItem;
    @Getter ActivityData activityData;
    Map<WatcherTriggerType, List<ActivityWatcher>> watchersMap = new HashMap<>();

    abstract public void onProtoBuild(PlayerActivityData playerActivityData, ActivityInfoOuterClass.ActivityInfo.Builder activityInfo);
    abstract public void onInitPlayerActivityData(PlayerActivityData playerActivityData);

    public void initWatchers(Map<WatcherTriggerType, ConstructorAccess<?>> activityWatcherTypeMap){
        activityData = GameData.getActivityDataMap().get(activityConfigItem.getActivityId());

        // add watcher to map by id
        activityData.getWatcherDataList().forEach(watcherData -> {
            var watcherType = activityWatcherTypeMap.get(watcherData.getTriggerConfig().getWatcherTriggerType());
            ActivityWatcher watcher;
            if(watcherType != null){
                watcher = (ActivityWatcher) watcherType.newInstance();
            }else{
                watcher = new DefaultWatcher();
            }

            watcher.setWatcherId(watcherData.getId());
            watcher.setActivityHandler(this);
            watcher.setActivityWatcherData(watcherData);
            watchersMap.computeIfAbsent(watcherData.getTriggerConfig().getWatcherTriggerType(), k -> new ArrayList<>());
            watchersMap.get(watcherData.getTriggerConfig().getWatcherTriggerType()).add(watcher);
        });
    }

    protected void triggerCondEvents(Player player){
        if(activityData == null){
            return;
        }
        val questManager = player.getQuestManager();
        activityData.getCondGroupId().forEach(condGroupId -> {
            val condGroup = GameData.getActivityCondGroupMap().get((int)condGroupId);
            if(condGroup != null)
                condGroup.getCondIds().forEach(condID -> questManager.queueEvent(QuestCond.QUEST_COND_ACTIVITY_COND, condID));
        });
    }

    private List<Integer> getActivityConditions(){
        if(activityData == null){
            return new ArrayList<>();
        }
        return activityData.getCondGroupId().stream().map(condGroupId -> GameData.getActivityCondGroupMap().get((int)condGroupId))
            .filter(Objects::nonNull)
            .map(ActivityCondGroup::getCondIds)
            .flatMap(Collection::stream)
            .toList();
    }

    // TODO handle possible overwrites
    private List<Integer> getMeetConditions(ActivityConditionExecutor conditionExecutor){
        return conditionExecutor.getMeetActivitiesConditions(getActivityConditions());
    }

    private Map<Integer, PlayerActivityData.WatcherInfo> initWatchersDataForPlayer(){
        return watchersMap.values().stream()
            .flatMap(Collection::stream)
            .map(PlayerActivityData.WatcherInfo::init)
            .collect(Collectors.toMap(PlayerActivityData.WatcherInfo::getWatcherId, y -> y));
    }

    public PlayerActivityData initPlayerActivityData(Player player){
        PlayerActivityData playerActivityData = PlayerActivityData.of()
            .activityId(activityConfigItem.getActivityId())
            .uid(player.getUid())
            .watcherInfoMap(initWatchersDataForPlayer())
            .build();

        onInitPlayerActivityData(playerActivityData);
        return playerActivityData;
    }

    public ActivityInfoOuterClass.ActivityInfo toProto(PlayerActivityData playerActivityData, ActivityConditionExecutor conditionExecutor){
        var proto = ActivityInfoOuterClass.ActivityInfo.newBuilder();
        proto.setActivityId(activityConfigItem.getActivityId())
            .setActivityType(activityConfigItem.getActivityType())
            .setScheduleId(activityConfigItem.getScheduleId())
            .setBeginTime(DateHelper.getUnixTime(activityConfigItem.getBeginTime()))
            .setFirstDayStartTime(DateHelper.getUnixTime(activityConfigItem.getBeginTime()))
            .setEndTime(DateHelper.getUnixTime(activityConfigItem.getEndTime()))
            .addAllMeetCondList(getMeetConditions(conditionExecutor));

        if (playerActivityData != null){
            proto.addAllWatcherInfoList(playerActivityData.getAllWatcherInfoList());
        }

        onProtoBuild(playerActivityData, proto);

        return proto.build();
    }

}
