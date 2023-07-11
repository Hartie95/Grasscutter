package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.game.dungeons.challenge.ChallengeInfo;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.challenge.enums.ChallengeType;
import emu.grasscutter.game.dungeons.challenge.trigger.TimeTrigger;
import emu.grasscutter.game.dungeons.challenge.trigger.KillMonsterTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.List;

public class KillMonsterTimeChallengeFactoryHandler implements ChallengeFactoryHandler{
    @Getter @Setter private ChallengeType challengeType;
    @Override
    public boolean isThisType(ChallengeType challengeType) {
        // ActiveChallenge with 180,180,45,133108061,1,0
        // ActiveChallenge Fast with 1001, 5, 15, 240004005, 10, 0
        if (challengeType == ChallengeType.CHALLENGE_KILL_COUNT_IN_TIME
            || challengeType == ChallengeType.CHALLENGE_KILL_COUNT_FAST) {
            setChallengeType(challengeType);
            return true;
        }
        return false;
    }

    public boolean shouldResetTimer(){
        return getChallengeType() == ChallengeType.CHALLENGE_KILL_COUNT_FAST;
    }

    /**
     * Build a new challenge
     * @param params: [timeLimit, groupId, targetCount, unused1]
     */
    @Override
    public WorldChallenge build(ChallengeInfo header, List<Integer> params, Scene scene, SceneGroup group) {
        val realGroup = scene.getScriptManager().getGroupById(params.get(1));
        return new WorldChallenge(
            scene, realGroup,
            header,
            List.of(params.get(2), params.get(0)), // parameters
            List.of(new KillMonsterTrigger(1, params.get(2), shouldResetTimer()), new TimeTrigger(2, params.get(0), true)),
            0, 0 // success count, fail count
        );
    }
}
