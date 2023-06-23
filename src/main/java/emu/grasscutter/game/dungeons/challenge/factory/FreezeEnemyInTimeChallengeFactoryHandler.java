package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.ElementReactionTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.InTimeTrigger;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_FREEZE_ENEMY_IN_TIME;
public class FreezeEnemyInTimeChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        return challengeType == CHALLENGE_FREEZE_ENEMY_IN_TIME;
    }

    /**
     * Build a new challenge
     * @param params: [timeLimit, goal, unused1, unused2, successCount, failCount]
     */
    @Override
    public WorldChallenge build(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            indices,
            List.of(), params.get(0), params.get(1), // parameters, time limit, goal
            List.of(new ElementReactionTrigger(0, List.of(ElementReactionType.Freeze)), new InTimeTrigger(0)),
            params.get(params.size() - 2), params.get(params.size() - 1) // success count, fail count
        );
    }
}
