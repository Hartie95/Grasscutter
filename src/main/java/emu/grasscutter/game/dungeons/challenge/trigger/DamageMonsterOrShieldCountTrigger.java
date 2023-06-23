package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class DamageMonsterOrShieldCountTrigger extends ChallengeTrigger{
    public DamageMonsterOrShieldCountTrigger(int paramIndex) {
        super(paramIndex);
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getScore().get()));
    }

    @Override
    public void onDamageMonsterOrShield(WorldChallenge challenge, float damage) {
        int oldScore = challenge.getScore().get();
        int newScore = challenge.getScore().addAndGet((int) damage);

        if (oldScore != newScore) {
            challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), newScore));
        }

        if(newScore >= challenge.getGoal()){
            challenge.done();
        }
    }
}
