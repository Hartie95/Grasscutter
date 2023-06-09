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
    private final int challengeId;
    private final int challengeIndex;
    private final List<Integer> paramList;
    private final int timeLimit;
    private final List<ChallengeTrigger> challengeTriggers;
    private boolean progress;
    private boolean success;
    private long startedAt;
    private int finishedTime;
    private final int goal;
    private final AtomicInteger score;

    public WorldChallenge(Scene scene, SceneGroup group,
                          int challengeId, int challengeIndex, List<Integer> paramList,
                          int timeLimit, int goal,
                          List<ChallengeTrigger> challengeTriggers){
        this.scene = scene;
        this.group = group;
        this.challengeId = challengeId;
        this.challengeIndex = challengeIndex;
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
        if(!inProgress()){
            return;
        }
        if(timeLimit <= 0){
            return;
        }
        challengeTriggers.forEach(t -> t.onCheckTimeout(this));
    }
    public void start(){
        if(inProgress()){
            Grasscutter.getLogger().info("Could not start a in progress challenge.");
            return;
        }
        this.progress = true;
        this.startedAt = getScene().getSceneTimeSeconds();
        getScene().broadcastPacket(new PacketDungeonChallengeBeginNotify(this));
        challengeTriggers.forEach(t -> t.onBegin(this));
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
                String.valueOf(getGroup().id),
                String.valueOf(getChallengeId())
            ));
        }

        this.getScene().getScriptManager().callEvent(
                // TODO record the time in PARAM2 and used in action
                new ScriptArgs(getGroupId(), EventType.EVENT_CHALLENGE_SUCCESS)
                    .setParam2(finishedTime)
                    .setEventSource(Integer.toString(getChallengeIndex())
                    ));
        this.getScene().triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_FINISH_CHALLENGE, getChallengeId(), getChallengeIndex());

        challengeTriggers.forEach(t -> t.onFinish(this));
    }

    public void fail(){
        if(!inProgress()){
            return;
        }
        finish(false);
        this.getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_CHALLENGE_FAIL)
            .setEventSource(Integer.toString(getChallengeIndex())));
        challengeTriggers.forEach(t -> t.onFinish(this));
    }

    private void finish(boolean success){
        this.progress = false;
        this.success = success;
        this.finishedTime = (int)(getScene().getSceneTimeSeconds() - this.startedAt);
        getScene().broadcastPacket(new PacketDungeonChallengeFinishNotify(this));
    }

    public int increaseScore(){
        return score.incrementAndGet();
    }
    public void onMonsterDeath(EntityMonster monster){
        if(!inProgress()){
            return;
        }
        if(monster.getGroupId() != getGroup().id){
            return;
        }
        this.challengeTriggers.forEach(t -> t.onMonsterDeath(this, monster));
    }
    public void onGadgetDeath(EntityGadget gadget){
        if(!inProgress()){
            return;
        }
        if(gadget.getGroupId() != getGroup().id){
            return;
        }
        this.challengeTriggers.forEach(t -> t.onGadgetDeath(this, gadget));
    }
    public void onGroupTriggerDeath(SceneTrigger trigger){
        if(!inProgress()){
            return;
        }
        val triggerGroup = trigger.getCurrentGroup();
        if(triggerGroup==null || triggerGroup.id != getGroup().id){
            return;
        }
        this.challengeTriggers.forEach(t -> t.onGroupTrigger(this, trigger));
    }

    public void onGadgetDamage(EntityGadget gadget){
        if(!inProgress()){
            return;
        }
        if(gadget.getGroupId() != getGroup().id){
            return;
        }
        this.challengeTriggers.forEach(t -> t.onGadgetDamage(this, gadget));
    }

    public int getGroupId(){
        return group.id;
    }
}
