package emu.grasscutter.game.dungeons.challenge;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.WatcherTriggerType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.SceneTrigger;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.packet.send.PacketDungeonChallengeBeginNotify;
import emu.grasscutter.server.packet.send.PacketDungeonChallengeFinishNotify;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class WorldChallenge {
    private final Scene scene;
    private final SceneGroup group;
    private final int challengeIndex;
    private final int challengeId;
    private final int fatherIndex;
    private final List<Integer> paramList;
    private final int timeLimit;
    private final List<ChallengeTrigger> challengeTriggers;
    private boolean progress;
    private boolean success;
    private int startedAt;
    private int finishedTime;
    private final int goal;
    private final AtomicInteger score;

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    public WorldChallenge(Scene scene, SceneGroup group,
                          List<Integer> indices,
                          List<Integer> paramList, int timeLimit, int goal,
                          List<ChallengeTrigger> challengeTriggers){
        this.scene = scene;
        this.group = group;
        this.challengeIndex = indices.get(0);
        this.challengeId = indices.get(1);
        this.fatherIndex = indices.get(2);
        this.paramList = paramList;
        this.timeLimit = timeLimit;
        this.challengeTriggers = challengeTriggers;
        this.goal = goal;
        this.score = new AtomicInteger(0);
    }
    public boolean inProgress(){
        return this.progress;
    }
    public void onCheckTimeOut(){
        if(!inProgress() || getTimeLimit() <= 0){
            return;
        }
        getChallengeTriggers().forEach(t -> t.onCheckTimeout(this));
    }
    public void start(){
        if(inProgress()){
            Grasscutter.getLogger().info("Could not start a in progress challenge.");
            return;
        }
        setProgress(true);
        setStartedAt(getScene().getSceneTimeSeconds());
        getScene().broadcastPacket(new PacketDungeonChallengeBeginNotify(this));
        getChallengeTriggers().forEach(t -> t.onBegin(this));
    }

    public void done(){
        if(!inProgress()){
            return;
        }
        finish(true);
        if (getScene().getDungeonManager() != null && getScene().getDungeonManager().getDungeonData() != null) {
            getScene().getPlayers().forEach(p -> p.getActivityManager().triggerWatcher(
                WatcherTriggerType.TRIGGER_FINISH_CHALLENGE,
                String.valueOf(getScene().getDungeonManager().getDungeonData().getId()),
                String.valueOf(getGroupId()),
                String.valueOf(getChallengeId())
            ));
        }

        getScene().getScriptManager().callEvent(
                // TODO record the time in PARAM2 and used in action
                new ScriptArgs(getGroupId(), EventType.EVENT_CHALLENGE_SUCCESS)
                    .setParam2(getFinishedTime())
                    .setEventSource(Integer.toString(getChallengeIndex())
                    ));
        getScene().triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_FINISH_CHALLENGE, getChallengeId(), getChallengeIndex());

        getChallengeTriggers().forEach(t -> t.onFinish(this));
    }

    public void fail(){
        if(!inProgress()){
            return;
        }
        finish(false);
        this.getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_CHALLENGE_FAIL)
            .setEventSource(Integer.toString(getChallengeIndex())));
        getChallengeTriggers().forEach(t -> t.onFinish(this));
    }

    protected void finish(boolean success){
        setProgress(false);
        setSuccess(success);
        setFinishedTime(getScene().getSceneTimeSeconds() - getStartedAt());
        getScene().broadcastPacket(new PacketDungeonChallengeFinishNotify(this));
    }

    public int increaseScore(){
        return getScore().incrementAndGet();
    }
    public void onMonsterDeath(EntityMonster monster){
        if(!inProgress() || monster.getGroupId() != getGroupId()){
            return;
        }
        getChallengeTriggers().forEach(t -> t.onMonsterDeath(this, monster));
    }

    public void onGadgetDeath(EntityGadget gadget){
        if(!inProgress() || gadget.getGroupId() != getGroupId()){
            return;
        }
        getChallengeTriggers().forEach(t -> t.onGadgetDeath(this, gadget));
    }
    public void onGroupTriggerDeath(SceneTrigger trigger){
        if(!inProgress()){
            return;
        }
        val triggerGroup = trigger.getCurrentGroup();
        if(triggerGroup == null || triggerGroup.id != getGroupId()){
            return;
        }
        getChallengeTriggers().forEach(t -> t.onGroupTrigger(this, trigger));
    }

    public void onGadgetDamage(EntityGadget gadget){
        if(!inProgress() || gadget.getGroupId() != getGroupId()){
            return;
        }
        getChallengeTriggers().forEach(t -> t.onGadgetDamage(this, gadget));
    }

    public int getGroupId(){
        return getGroup().id;
    }
}
