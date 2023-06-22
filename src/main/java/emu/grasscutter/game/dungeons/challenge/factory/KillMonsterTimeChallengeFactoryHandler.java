package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.InTimeTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterCountTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.val;

import java.util.List;

public class KillMonsterTimeChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // ActiveChallenge with 180,180,45,133108061,1,0
        // ActiveChallenge Fast with 1001, 5, 15, 240004005, 10, 0
        return challengeType == ChallengeType.CHALLENGE_KILL_COUNT_IN_TIME ||
            challengeType == ChallengeType.CHALLENGE_KILL_COUNT_FAST;
    }

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    // params: [timeLimit, groupId, targetCount, unused1]
    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(1));
        return new WorldChallenge(
            scene, realGroup,
            indices,
            List.of(params.get(2), params.get(0)),
            params.get(0), // Limit
            params.get(2),  // Goal
            List.of(new KillMonsterCountTrigger(1), new InTimeTrigger(2))
        );
    }
}
