package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.scripts.data.SceneTrigger;
import lombok.Getter;

@Getter
public class TriggerGroupTriggerTrigger extends ChallengeTrigger{
    private final String triggerTag;

    public TriggerGroupTriggerTrigger(int paramIndex, int goal, int triggerTag){
        super(paramIndex, goal);
        this.triggerTag = String.valueOf(triggerTag);
    }

    @Override
    public void onGroupTrigger(WorldChallenge challenge, SceneTrigger trigger) {
        if(!getTriggerTag().equals(trigger.getTag())) return;

        int newScore = getScore().incrementAndGet();
        onBegin(challenge);

        if(newScore >= getGoal().get()) challenge.done();
    }
}
