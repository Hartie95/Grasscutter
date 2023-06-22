package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class DamageMonsterCountTrigger extends ChallengeTrigger{
    public DamageMonsterCountTrigger(int paramIndex) {
        super(paramIndex);
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getScore().get()));
    }

    @Override
    public void onMonsterDamage(WorldChallenge challenge, EntityMonster monster, float amount) {
        int oldScore = challenge.getScore().get();
        int newScore = challenge.getScore().addAndGet((int) amount);

        if (oldScore != newScore) {
            challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), newScore));
        }

        if(newScore >= challenge.getGoal()){
            challenge.done();
        }
    }
}
