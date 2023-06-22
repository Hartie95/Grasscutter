package emu.grasscutter.game.dungeons.challenge;

import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class FatherChallenge extends WorldChallenge{
    private AtomicInteger failScore;
    private final List<ChildChallenge> childChallenge = new ArrayList<>();
    public FatherChallenge(Scene scene, SceneGroup group,
                           List<Integer> indices, List<Integer> paramList,
                           int timeLimit, int goal,
                           List<ChallengeTrigger> challengeTriggers) {
        super(scene, group, indices, paramList, timeLimit, goal, challengeTriggers);
    }

    public void attachChild(WorldChallenge childChallenge) {
        ChildChallenge child = (ChildChallenge) childChallenge;
        getChildChallenge().add(child);
        child.addFather(this);
    }

    @Override
    public void start() {
        super.start();
        getChildChallenge().forEach(WorldChallenge::start);
    }

    @Override
    public void fail() {
        super.fail();
        getChildChallenge().forEach(ChildChallenge::fail);
    }

    public int  incSuccessScore(int score) {
        return getScore().addAndGet(score);
    }

    public int  incFailScore(int score) {
        return getFailScore().addAndGet(score);
    }

    public void onMonsterDamage(EntityMonster monster, float damageAmount){
        if(!inProgress() || monster.getGroupId() != getGroupId()){
            return;
        }
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onMonsterDamage(cc, monster, damageAmount)));
    }

    public void onElementReactionCount(EntityMonster monster, ElementReactionType reactionType){
        if(!inProgress() || monster.getGroupId() != getGroupId()){
            return;
        }
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onElementReactionCount(cc, monster, reactionType)));
    }

    public void onFreezeInTime(EntityMonster monster, ElementReactionType reactionType){
        if(!inProgress() || monster.getGroupId() != getGroupId()){
            return;
        }
        getChildChallenge().forEach(cc -> cc.getChallengeTriggers()
            .forEach(t -> t.onFreezeInTime(cc, monster, reactionType)));
    }

    public void onIncFailSuccScore(int index, int score) {
        getChallengeTriggers().forEach(t -> t.onIncFailSuccScore(this, index, score));
    }
}
