package emu.grasscutter.game.dungeons.challenge;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final AtomicInteger failScore;
    private final List<WorldChallenge> childChallenge = new ArrayList<>();
    private WorldChallenge fatherChallenge = null;
    private final int successCount;
    private final int failCount;

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    public WorldChallenge(Scene scene, SceneGroup group,
                          List<Integer> indices,
                          List<Integer> paramList, int timeLimit, int goal,
                          List<ChallengeTrigger> challengeTriggers,
                          int successCount, int failCount){
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
        this.failScore = new AtomicInteger(0);
        this.successCount = successCount;
        this.failCount = failCount;
    }

    /**
     * Return if challenge is in progress
     */
    public boolean inProgress(){
        return this.progress;
    }

    /**
     * Get the monster group id spawned by the challenge
     */
    public int getGroupId(){
        return getGroup() == null ? 0 : getGroup().id;
    }

    /**
     * Attach a child challenge to this challenge,
     * also set this as father challenge of the child challenge
     * @param childChallenge child challenge to attach
     */
    public void attachChild(WorldChallenge childChallenge) {
        getChildChallenge().add(childChallenge);
        childChallenge.setFatherChallenge(this);
    }

    /**
     * Starts the challenge
     */
    public void start(){
        if(inProgress()){
            Grasscutter.getLogger().info("Could not start a in progress challenge.");
            return;
        }
        setProgress(true);
        setStartedAt(getScene().getSceneTimeSeconds());
        getScene().broadcastPacket(new PacketDungeonChallengeBeginNotify(this));
        getChallengeTriggers().forEach(t -> t.onBegin(this));
        getChildChallenge().forEach(WorldChallenge::start); // child challenges will have empty list here
    }

    /**
     * Finishes the challenge
     */
    public void done(){
        if(!inProgress()) return;

        finish(true);
        getChallengeTriggers().forEach(t -> t.onFinish(this));
        if (getFatherChallenge() != null) return; // means that this is a child challenge

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
        getScene().triggerDungeonEvent(
            DungeonPassConditionType.DUNGEON_COND_FINISH_CHALLENGE, getChallengeId(), getChallengeIndex());
    }

    /**
     * Fails the challenge
     */
    public void fail(){
        if(!inProgress()) return;

        finish(false);
        getChallengeTriggers().forEach(t -> t.onFinish(this));
        if (getFatherChallenge() != null) return; // means that this is a child challenge

        this.getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_CHALLENGE_FAIL)
            .setEventSource(Integer.toString(getChallengeIndex())));

        getChildChallenge().forEach(WorldChallenge::fail);
    }

    /**
     * Set challenge properties as it finishes
     * @param success true = challenge success, false = challenge fails
     */
    protected void finish(boolean success){
        setProgress(false);
        setSuccess(success);
        setFinishedTime(getScene().getSceneTimeSeconds() - getStartedAt());
        getScene().broadcastPacket(new PacketDungeonChallengeFinishNotify(this));
        Optional.ofNullable(getFatherChallenge()).ifPresent(fc ->
            fc.onIncFailSuccScore(success ? 1 : 2, success ? getSuccessCount() : getFailCount()));
    }

    /**
     * Invoke whenever a goal has been met partially
     */
    public int increaseScore(){
        return getScore().incrementAndGet();
    }

    /**
     * Invoke whenever a child challenge has finished
     * @param score child challenge success count to increase
     */
    public int increaseScore(int score){
        return getScore().addAndGet(score);
    }

    /**
     * Invoke whenever a child challenge has failed
     * @param score child challenge fail count to increase
     */
    public int  incFailScore(int score) {
        return getFailScore().addAndGet(score);
    }

    /**
     * Constantly checking if challenge has timed out
     */
    public void onCheckTimeOut(){
        if(!inProgress() || getTimeLimit() <= 0) return;

        getChallengeTriggers().forEach(t -> t.onCheckTimeout(this));
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onCheckTimeout(cc)));
    }

    /**
     * Invoke when player killed a monster
     * @param monster Monster that belongs to the group spawned by the challenge
     */
    public void onMonsterDeath(EntityMonster monster){
        if(!inProgress() || monster.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onMonsterDeath(this, monster));
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onMonsterDeath(cc, monster)));
    }

    public void onGadgetDeath(EntityGadget gadget){
        if(!inProgress() || gadget.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onGadgetDeath(this, gadget));
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onGadgetDeath(cc, gadget)));
    }

    public void onGroupTriggerDeath(SceneTrigger trigger){
        if(!inProgress())return;

        val triggerGroup = trigger.getCurrentGroup();
        if(triggerGroup == null || triggerGroup.id != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onGroupTrigger(this, trigger));
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onGroupTrigger(cc, trigger)));
    }

    public void onGadgetDamage(EntityGadget gadget){
        if(!inProgress() || gadget.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onGadgetDamage(this, gadget));
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onGadgetDamage(cc, gadget)));
    }

    /**
     * Invoke when player triggered elemental reaction
     * @param monster Monster that belongs to the group spawned by the challenge
     * @param reactionType Reaction triggered by attacker
     */
    public void onElementReaction(EntityMonster monster, ElementReactionType reactionType){
        if(!inProgress() || monster.getGroupId() != getGroupId()){
            return;
        }
        getChallengeTriggers().forEach(t -> t.onElementReaction(this, reactionType));
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onElementReaction(cc, reactionType)));
    }

    /**
     * Invoke when player damaging monster or monster damaging player's shield
     * @param monster Monster that belongs to the group spawned by the challenge
     * @param damage Damage dealt
     */
    public void onDamageMonsterOrShield(EntityMonster monster, float damage) {
        if(!inProgress() || monster.getGroupId() != getGroupId()){
            return;
        }
        getChallengeTriggers().forEach(t -> t.onDamageMonsterOrShield(this, damage));
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onDamageMonsterOrShield(cc, damage)));
    }

    /**
     * Invoke when child challenge finishes or fails
     * @param index 1 = success, 2 = fail
     * @param score Score to increase
     */
    public void onIncFailSuccScore(int index, int score) {
        getChallengeTriggers().forEach(t -> t.onIncFailSuccScore(this, index, score));
    }
}
