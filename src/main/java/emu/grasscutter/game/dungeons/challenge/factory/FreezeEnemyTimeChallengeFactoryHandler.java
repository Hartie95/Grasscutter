package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.ChallengeScoreInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.ElementReactionTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.TimeTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;

import java.util.List;

import static emu.grasscutter.game.props.ElementReactionType.Freeze;
import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_FREEZE_ENEMY_IN_TIME;
public class FreezeEnemyTimeChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        return challengeType == CHALLENGE_FREEZE_ENEMY_IN_TIME;
    }

    /**
     * Build a new challenge
     * @param params: [timeLimit, goal, unused1, unused2]
     */
    @Override
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, ChallengeScoreInfo scoreInfo, Scene scene, SceneGroup group) {
        return new WorldChallenge(
            scene, group,
            header,
            List.of(), // parameters
            List.of(new ElementReactionTrigger(0, params.get(1), Freeze), new TimeTrigger(0, params.get(0))),
            scoreInfo
        );
    }
}
