package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.ChallengeScoreInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

public class Trigger2Trigger1ChallengeFactoryHandler implements ChallengeFactoryHandler {
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // Start challenge with 666,203,{ 26,888,8,2,999,1 }
        return challengeType == ChallengeType.CHALLENGE_TRIGGER2_AVOID_TRIGGER1;
    }

    /**
     * Build a new challenge
     * @param params: [platformGoalPoint, triggerTag2, triggerCount2, unused1, triggerTag1, triggerCount1]
     */
    @Override
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, ChallengeScoreInfo scoreInfo, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            header,
            List.of(), // parameters
            List.of(),
            scoreInfo
        );
    }
}
