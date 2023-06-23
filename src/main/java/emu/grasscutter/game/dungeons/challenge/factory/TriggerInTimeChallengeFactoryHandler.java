package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.InTimeTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.TriggerGroupTriggerTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_TRIGGER_IN_TIME;

public class TriggerInTimeChallengeFactoryHandler implements ChallengeFactoryHandler {
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // kill gadgets(explosive barrel) in time
        // ActiveChallenge with 56,201,20,2,201,4
        // open chest in time
        // ActiveChallenge with 666,202,30,7,202,1
        return challengeType == CHALLENGE_TRIGGER_IN_TIME;
    }

    /**
     * Build a new challenge
     * @param params: [timeLimit, unused1, triggerTag, triggerCount]
     */
    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            indices,
            List.of(params.get(0), params.get(3)), params.get(0), params.get(3), // parameters, time limit, goal
            List.of(new InTimeTrigger(1), new TriggerGroupTriggerTrigger(2, Integer.toString(params.get(2)))),
            0, 0 // success count, fail count
        );
    }
}
