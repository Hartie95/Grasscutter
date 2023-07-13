package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.ChallengeScoreInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

public interface ChallengeFactoryHandler {
    /**
     * Check if the incoming challenge config meet this challenge type
     * @param challengeType challenge type to compare
     */
    boolean isThisType(ChallengeType challengeType);

    /**
     * Build a new challenge
     * @param header: currentChallengeIndex, currentChallengeId, fatherChallengeIndex
     * @param params: [Different parameters depending on challenge type, size varies from 3 to 7]
     * @param scene: Current scene that player is in
     * @param group: Group spawned/attached to the challenge
     */
    WorldChallenge build(ChallengeInfo header, List<Integer> params, ChallengeScoreInfo scoreInfo, Scene scene, SceneGroup group);
}
