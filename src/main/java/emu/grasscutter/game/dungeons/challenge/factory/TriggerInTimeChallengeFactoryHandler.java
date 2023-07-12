package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.TimeTrigger;
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
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            header,
            List.of(params.get(0), params.get(3)), // parameters
            List.of(new TimeTrigger(1, params.get(0)), new TriggerGroupTriggerTrigger(2, params.get(3), params.get(2))),
            0, 0 // success count, fail count
        );
    }
}
