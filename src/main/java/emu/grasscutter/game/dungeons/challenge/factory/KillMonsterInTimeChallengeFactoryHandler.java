package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.InTimeTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.val;

import java.util.List;

public class KillMonsterInTimeChallengeFactoryHandler  implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // ActiveChallenge with 180, 72, 240, 133220161, 133220161, 0
        return challengeType == ChallengeType.CHALLENGE_KILL_MONSTER_IN_TIME;
    }

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    // params: [timeLimit, groupId, targetCfgId, unused1]

    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(1));
        return new WorldChallenge(
            scene, realGroup,
            indices,
            List.of(params.get(0)),
            params.get(0), // Limit
            0,  // Goal
            List.of(new KillMonsterTrigger(params.get(2)), new InTimeTrigger(1))
        );
    }
}
