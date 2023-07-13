package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.ChallengeScoreInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.TimeTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.TriggerGroupTriggerTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_TRIGGER_IN_TIME_FLY;

public class TriggerTimeFlyChallengeFactoryHandler implements ChallengeFactoryHandler{

    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // ActiveChallenge with 888,189,25,4,666,5
        return challengeType == CHALLENGE_TRIGGER_IN_TIME_FLY;
    }

    /**
     * Build a new challenge
     * @param params: [timeLimit, unused1, triggerTag, incTimerCount]
     */
    @Override
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, ChallengeScoreInfo scoreInfo, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            header,
            List.of(params.get(0), params.get(0)), // parameters
            List.of(new TimeTrigger(2, params.get(0)), new TriggerGroupTriggerTrigger(0, 1, params.get(2)), new KillMonsterTrigger(0, Integer.MAX_VALUE, params.get(3))),
            scoreInfo
        );
    }
}
