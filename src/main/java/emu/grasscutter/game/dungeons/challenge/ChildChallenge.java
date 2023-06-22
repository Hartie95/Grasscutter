package emu.grasscutter.game.dungeons.challenge;

import emu.grasscutter.game.dungeons.challenge.trigger.ChallengeTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChildChallenge extends WorldChallenge{
    private final int successCount;
    private final int failCount;
    private FatherChallenge fatherChallenge;

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    public ChildChallenge(Scene scene, SceneGroup group,
                          List<Integer> indices,
                          List<Integer> paramList, int timeLimit, int goal,
                          List<ChallengeTrigger> challengeTriggers, int successCount, int failCount) {
        super(scene, group,indices, paramList, timeLimit, goal, challengeTriggers);

        this.successCount = successCount;
        this.failCount = failCount;
    }

    @Override
    public void done() {
        if(!inProgress()){
            return;
        }
        finish(true);
    }

    @Override
    public void fail() {
        if(!inProgress()){
            return;
        }
        finish(false);
    }

    @Override
    protected void finish(boolean success) {
        super.finish(success);
        getFatherChallenge().onIncFailSuccScore(success ? 1 : 2, success ? getSuccessCount() : getFailCount());
    }

    public void addFather(FatherChallenge fatherChallenge) {
        setFatherChallenge(fatherChallenge);
    }
}
