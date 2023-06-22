package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChildChallenge;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.DamageMonsterCountTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_MONSTER_DAMAGE_COUNT;

public class DamageMonsterCountChallengeFactoryHandler implements ChallengeFactoryHandler {
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        return challengeType == CHALLENGE_MONSTER_DAMAGE_COUNT;
    }

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    // params: [damageCount(total/goal), unused1, unused2, unused3, successCount, failCount]
    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        return new ChildChallenge(
            scene, group,
            indices,
            List.of(params.get(0)),
            0,
            params.get(0),
            List.of(new DamageMonsterCountTrigger(1)),
            params.get(4), params.get(5)
        );
    }
}
