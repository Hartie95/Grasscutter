package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.GuardTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterCountTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.val;

import java.util.List;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_KILL_COUNT_GUARD_HP;

public class KillAndGuardChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // ActiveChallenge with 1,188,234101003,12,3030,0
        return challengeType == CHALLENGE_KILL_COUNT_GUARD_HP;
    }

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    // params: [groupId, monstersToKill, gadgetCFGId, unused]

    @Override /*TODO check param4 == monstersToKill*/
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(0));
        return new WorldChallenge(
            scene, realGroup,
            indices,
            List.of(params.get(1), 100),
            0, // Limit
            params.get(1),  // Goal
            List.of(new KillMonsterCountTrigger(1), new GuardTrigger(2, params.get(2)))
        );
    }
}
