package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

public interface ChallengeFactoryHandler {
    boolean isThisType(ChallengeType challengeType);
    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    // params: [Different parameters depending on challenge type]
    WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group);
}
