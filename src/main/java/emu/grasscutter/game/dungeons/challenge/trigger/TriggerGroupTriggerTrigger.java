package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.scripts.data.SceneTrigger;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

public class TriggerGroupTriggerTrigger extends ChallengeTrigger{
    @Getter private final String triggerTag;

    public TriggerGroupTriggerTrigger(int paramIndex, String triggerTag){
        super(paramIndex);
        this.triggerTag = triggerTag;
    }
    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), challenge.getScore().get()));
    }

    @Override
    public void onGroupTrigger(WorldChallenge challenge, SceneTrigger trigger) {
        if(!getTriggerTag().equals(trigger.getTag())) {
            return;
        }

        var newScore = challenge.increaseScore();
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), newScore));
        if(newScore >= challenge.getGoal()){
            challenge.done();
        }
    }
}
