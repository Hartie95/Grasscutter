package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

public class TimeTrigger extends ChallengeTrigger{
    @Getter private final boolean RUN_FAIL;

    public TimeTrigger(int paramIndex, int goal){
        this(paramIndex, goal, true);
    }

    /**
     * Used when challenge have time limit.
     * shouldFail == false represents formal 'ForTimeTrigger'
     * shouldFail == true represents formal 'InTimeTrigger'
     * */
    public TimeTrigger(int paramIndex, int goal, boolean shouldFail){
        super(paramIndex, goal);
        this.RUN_FAIL = shouldFail;
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        if (getParamIndex() < 1) return; // when challenge is FreezeInTime

        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getStartedAt() + getGoal().get()));
    }

    @Override
    public void onCheckTimeout(WorldChallenge challenge) {
        if(challenge.getScene().getSceneTimeSeconds() - challenge.getStartedAt() < getGoal().get()) return;

        if (getParamIndex() < 1) {// when challenge is FreezeInTime
            challenge.getChallengeTriggers().stream()
                .filter(t -> t instanceof ElementReactionTrigger)
                .map(ElementReactionTrigger.class::cast)
                .peek(reactionTrigger -> reactionTrigger.getScore().set(0))
                .peek(reactionTrigger -> reactionTrigger.onBegin(challenge))
                .forEach(c -> challenge.setStartedAt(challenge.getScene().getSceneTimeSeconds()));
            return;
        }

        if (isRUN_FAIL()) challenge.fail(); else challenge.done();
    }
}
