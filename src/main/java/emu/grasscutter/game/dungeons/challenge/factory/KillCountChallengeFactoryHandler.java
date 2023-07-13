package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.ChallengeScoreInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.val;

import java.util.List;

public class KillCountChallengeFactoryHandler implements ChallengeFactoryHandler{
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
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, ChallengeScoreInfo scoreInfo, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(0));
        return new WorldChallenge(
            scene, realGroup,
            header,
            List.of(params.get(1)), // parameters
            List.of(new KillMonsterTrigger(1, params.get(1))),
            scoreInfo
        );
    }
}
