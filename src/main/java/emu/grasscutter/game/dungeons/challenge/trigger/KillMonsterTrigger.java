package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import lombok.Getter;


public class KillMonsterTrigger extends ChallengeTrigger{
    /**
     * For challenge type i.e. "KILL_COUNT_FAST", should reset or increment
     * timer after killing one monster
     * */
    @Getter private final boolean RESET_TIMER;
    @Getter private final int INC_TIMER_COUNT;

    public KillMonsterTrigger(int paramIndex, int goal){
        this(paramIndex, goal, false);
    }

    public KillMonsterTrigger(int paramIndex, int goal, boolean resetTimer){
        this(paramIndex, goal, resetTimer, 0);
    }

    public KillMonsterTrigger(int paramIndex, int goal, int incTimerCount){
        this(paramIndex, goal, true, incTimerCount);
    }

    /**
     * Used when challenge requires player to kill specific (amount of) monster.
     * Goal will be monster's config id if killing only one monster,
     * otherwise will be the number of monsters to kill.
     * Will also reset/increment time limit for specific challenges
     * */
    public KillMonsterTrigger(int paramIndex, int goal, boolean resetTimer, int incTimerCount){
        super(paramIndex, goal);
        this.RESET_TIMER = resetTimer;
        this.INC_TIMER_COUNT = incTimerCount;
    }

    @Override
    public void onMonsterDeath(WorldChallenge challenge, EntityMonster monster) {
        if(monster.getConfigId() == getGoal().get()) { // for challenge killing specific monster
            challenge.done();
            return;
        }

        int newScore = getScore().incrementAndGet();
        onBegin(challenge);

        if(newScore >= getGoal().get()) {
            challenge.done();
            return;
        }

        if (!isRESET_TIMER()) return;

        TimeTrigger trigger = challenge.getChallengeTriggers().stream()
            .filter(t -> t instanceof TimeTrigger)
            .map(TimeTrigger.class::cast)
            .findFirst().orElse(null);
        if (trigger == null) return;

        if (getINC_TIMER_COUNT() > 0) {
            // reset time count partially
            trigger.getGoal().addAndGet(getINC_TIMER_COUNT());
        } else {
            // reset time count
            challenge.setStartedAt(challenge.getScene().getSceneTimeSeconds());
        }

        trigger.onBegin(challenge);
    }
}
