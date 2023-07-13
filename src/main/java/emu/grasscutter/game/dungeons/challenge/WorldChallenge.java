package emu.grasscutter.game.dungeons.challenge;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.GameEntity;
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

@Getter
@Setter
public class WorldChallenge {
    private final Scene scene;
    private final SceneGroup group;
    private final ChallengeInfo info;
    private final List<Integer> paramList;
    private final List<ChallengeTrigger> challengeTriggers;
    private boolean progress;
    private boolean success;
    private int startedAt;
    private int finishedTime;
    /**
     * Father and child challenge related info
     * */
    private final List<WorldChallenge> childChallenge = new ArrayList<>();
    private WorldChallenge fatherChallenge = null;
    private final ChallengeScoreInfo scoreInfo;

    // info: currentChallengeIndex, currentChallengeId, fatherChallengeIndex
    // scoreInfo: success count, fail count
    public WorldChallenge(Scene scene, SceneGroup group,
                          ChallengeInfo info,
                          List<Integer> paramList,
                          List<ChallengeTrigger> challengeTriggers,
                          ChallengeScoreInfo scoreInfo){
        this.scene = scene;
        this.group = group;
        this.info = info;
        this.paramList = paramList;
        this.challengeTriggers = challengeTriggers;
        this.scoreInfo = scoreInfo;
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

        if (inProgress()) childChallenge.start(); // some child challenges will be added after father challenge started
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

        if (getScene().getDungeonManager() != null && getScene().getDungeonManager().getDungeonData() != null) {
            getScene().getPlayers().forEach(p -> p.getActivityManager().triggerWatcher(
                WatcherTriggerType.TRIGGER_FINISH_CHALLENGE,
                String.valueOf(getScene().getDungeonManager().getDungeonData().getId()),
                String.valueOf(getGroupId()),
                String.valueOf(getInfo().challengeId())
            ));
        }

        getScene().getScriptManager().callEvent(
                // TODO record the time in PARAM2 and used in action
                new ScriptArgs(getGroupId(), EventType.EVENT_CHALLENGE_SUCCESS)
                    .setParam2(getFinishedTime())
                    .setEventSource(Integer.toString(getInfo().challengeIndex())
                    ));
        getScene().triggerDungeonEvent(
            DungeonPassConditionType.DUNGEON_COND_FINISH_CHALLENGE,
            getInfo().challengeId(), getInfo().challengeIndex());
    }

    /**
     * Fails the challenge
     */
    public void fail(){
        if(!inProgress()) return;

        finish(false);
        getChallengeTriggers().forEach(t -> t.onFinish(this));

        this.getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_CHALLENGE_FAIL)
            .setEventSource(Integer.toString(getInfo().challengeIndex())));

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
            fc.onIncFailSuccScore(success, getScoreInfo().get(success)));
    }

    /**
     * Constantly checking if challenge has timed out
     */
    public void onCheckTimeOut(){
        if(!inProgress()) return;

        getChallengeTriggers().forEach(t -> t.onCheckTimeout(this));
        getChildChallenge().stream().filter(WorldChallenge::inProgress)
            .forEach(cc -> cc.getChallengeTriggers().forEach(t -> t.onCheckTimeout(cc)));
    }

    /**
     * Invoke when player killed a monster
     * @param monster Monster that belongs to the group spawned by the challenge
     */
    public void onMonsterDeath(EntityMonster monster){
        if(!inProgress() || monster.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onMonsterDeath(this, monster));
        getChildChallenge().stream().filter(WorldChallenge::inProgress)
            .forEach(cc -> cc.getChallengeTriggers().forEach(t -> t.onMonsterDeath(cc, monster)));
    }

    public void onGadgetDeath(EntityGadget gadget){
        if(!inProgress() || gadget.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onGadgetDeath(this, gadget));
        getChildChallenge().stream().filter(WorldChallenge::inProgress)
            .forEach(cc -> cc.getChallengeTriggers().forEach(t -> t.onGadgetDeath(cc, gadget)));
    }

    public void onGroupTriggerDeath(SceneTrigger trigger){
        if(!inProgress())return;

        val triggerGroup = trigger.getCurrentGroup();
        if(triggerGroup == null || triggerGroup.id != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onGroupTrigger(this, trigger));
        getChildChallenge().stream().filter(WorldChallenge::inProgress)
            .forEach(cc -> cc.getChallengeTriggers().forEach(t -> t.onGroupTrigger(cc, trigger)));
    }

    public void onGadgetDamage(EntityGadget gadget){
        if(!inProgress() || gadget.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onGadgetDamage(this, gadget));
        getChildChallenge().stream().filter(WorldChallenge::inProgress)
            .forEach(cc -> cc.getChallengeTriggers().forEach(t -> t.onGadgetDamage(cc, gadget)));
    }

    /**
     * Invoke when player triggered elemental reaction
     * @param defender target that element reaction invoked on
     * @param reactionType Reaction triggered by attacker
     */
    public void onElementReaction(GameEntity defender, ElementReactionType reactionType){
        if (!inProgress()) return;
        if (defender instanceof EntityMonster monster && monster.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onElementReaction(this, defender, reactionType));
        getChildChallenge().stream().filter(WorldChallenge::inProgress)
            .forEach(cc -> cc.getChallengeTriggers().forEach(t -> t.onElementReaction(cc, defender, reactionType)));
    }

    /**
     * Invoke when player damaging monster or monster damaging player's shield
     * @param entity Monster that belongs to the group spawned by the challenge
     * @param damage Damage dealt
     */
    public void onDamageMonsterOrShield(GameEntity entity, float damage) {
        if(!inProgress()) return;
        if (entity instanceof EntityMonster monster && monster.getGroupId() != getGroupId()) return;

        getChallengeTriggers().forEach(t -> t.onDamageMonsterOrShield(this, damage));
        getChildChallenge().stream().filter(WorldChallenge::inProgress)
            .forEach(cc -> cc.getChallengeTriggers().forEach(t -> t.onDamageMonsterOrShield(cc, damage)));
    }

    /**
     * Invoke when CHILD challenge finishes or fails
     */
    public void onIncFailSuccScore(boolean useSucc, int score) {
        getChallengeTriggers().forEach(t -> t.onIncFailSuccScore(this, useSucc, score));
    }
}
