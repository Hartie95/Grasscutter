package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.FatherTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.InTimeTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_FATHER_SUCC_IN_TIME;

public class FatherSuccessInTimeChallengeFactoryHandler implements ChallengeFactoryHandler{

    @Override
    public boolean isThisType(ChallengeType challengeType) {
        return challengeType == CHALLENGE_FATHER_SUCC_IN_TIME;
    }

    /**
     * Build a new challenge
     * @param params: [successCount, failCount, timeLimit]
     */
    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            indices,
            params, params.get(2), 0, // parameters, time limit, goal
            List.of(new FatherTrigger(), new InTimeTrigger(3)),
            params.get(0), params.get(1) // success count, fail count
        );
    }
}
