package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.GuardTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.TimeTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.val;

import java.util.List;
import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_GUARD_HP;

public class KillAndGuardInTimeChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // StartChallenge with 666, 251,{ 120,133210421,421007,0,5 } or
        // ActiveChallenge with 666, 12, 30, 111101015, 15019, 90
        return challengeType == CHALLENGE_GUARD_HP;
    }

    /**
     * Build a new challenge
     * @param params: [timeLimit, groupId, gadgetCFGId, unused1, incTimerCount(pretty sure)] or
     *                [timeLimit, groupId, gadgetCFGId, incTimerCount(pretty sure)]
     */
    @Override
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(1));
        int goal = realGroup == null || realGroup.monsters == null ? 0 : realGroup.monsters.size();

        return new WorldChallenge(
            scene, realGroup,
            header,
            List.of(100, params.get(0), params.get(params.size()-1)), // parameters
            List.of(new GuardTrigger(1, params.get(2)), new TimeTrigger(2, params.get(0)), new KillMonsterTrigger(3, goal, params.get(params.size()-1))),
            0, 0 // success count, fail count
        );
    }
}

