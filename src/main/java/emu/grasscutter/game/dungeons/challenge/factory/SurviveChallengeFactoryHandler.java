package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.ForTimeTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_SURVIVE;

public class SurviveChallengeFactoryHandler implements ChallengeFactoryHandler {
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // grp 201055005
        // ActiveChallenge with 100, 56, 60, 0, 0, 0
        return challengeType == CHALLENGE_SURVIVE;
    }

    /**
     * Build a new challenge
     * @param params: [timeToSurvive, unused1, unused2, unused3]
     */
    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            indices,
            List.of(params.get(0)), params.get(0), 0, // parameters, time limit, goal
            List.of(new ForTimeTrigger(1)),
            0, 0 // success count, fail count
        );
    }
}
