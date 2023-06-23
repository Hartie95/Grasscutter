package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterCountTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.val;

import java.util.List;

public class KillMonsterCountChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // ActiveChallenge with 1, 1, 241033003, 15, 0, 0
        return challengeType == ChallengeType.CHALLENGE_KILL_COUNT;
    }

    /**
     * Build a new challenge
     * @param params: [groupId, goal, unused1, unused2]
     */
    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(0));
        return new WorldChallenge(
            scene, realGroup,
            indices,
            List.of(params.get(1)), 0, params.get(1), // parameters, time limit, goal
            List.of(new KillMonsterCountTrigger(1)),
            0, 0 // success count, fail count
        );
    }
}
