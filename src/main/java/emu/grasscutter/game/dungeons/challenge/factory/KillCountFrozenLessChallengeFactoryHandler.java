package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.ElementReactionTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterTrigger;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.val;

import java.util.List;

import static emu.grasscutter.game.props.ElementReactionType.Freeze;
public class KillCountFrozenLessChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // ActiveChallenge with 2, 6, 240014003, 7, 5, 0
        return challengeType == ChallengeType.CHALLENGE_KILL_COUNT_FROZEN_LESS;
    }

    /**
     * Build a new challenge
     * @param params: [groupId, monsterCountToKill, maximumGotFrozenCount, unused1]
     */
    @Override
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(0));
        return new WorldChallenge(
            scene, realGroup,
            header,
            List.of(params.get(1), params.get(2)), // parameters
            List.of(new KillMonsterTrigger(1, params.get(1), false), new ElementReactionTrigger(2, params.get(2), Freeze, EntityAvatar.class, false)),
            0, 0 // success count, fail count
        );
    }
}
