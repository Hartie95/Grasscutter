package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

public class KillMonsterTrigger extends ChallengeTrigger{
    /**
     * For challenge type "KILL_COUNT_FAST", should reset or increment
     * timer after killing one monster
     * */
    @Getter private final boolean RESET_TIMER;

    /**
     * Used when challenge requires player to kill specific (amount of) monster.
     * Goal will be monster's config id if killing only one monster,
     * otherwise will be the number of monsters to kill.
     * */
    public KillMonsterTrigger(int paramIndex, int goal, boolean resetTimer){
        super(paramIndex, goal);
        this.RESET_TIMER = resetTimer;
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), getScore().get()));
    }

    @Override
    public void onMonsterDeath(WorldChallenge challenge, EntityMonster monster) {
        if(monster.getConfigId() == getGoal()) { // for challenge killing specific monster
            challenge.done();
            return;
        }

        int newScore = getScore().incrementAndGet();
        onBegin(challenge);

        if(newScore >= getGoal()) challenge.done();

        if (!isRESET_TIMER()) return;

        // reset time count
        challenge.setStartedAt(challenge.getScene().getSceneTimeSeconds());
        challenge.getChallengeTriggers().stream()
            .filter(t -> t instanceof TimeTrigger)
            .forEach(t -> t.onBegin(challenge));
    }
}
