package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class DamageCountTrigger extends ChallengeTrigger{

    /**
     * Used when challenge requires player damage monsters or defend(shield) against monster attack
     * */
    public DamageCountTrigger(int paramIndex, int goal) {
        super(paramIndex, goal);
    }

    @Override
    public void onDamageMonsterOrShield(WorldChallenge challenge, float damage) {
        // Entity.damage() should have checked against the damage type
        int oldScore = getScore().getAndAdd((int) damage);

        // clip sent damage to not exceed goal
        if (oldScore != getScore().get())
            challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
                challenge, getParamIndex(), Math.min(getScore().get(), getGoal().get())));

        if(getScore().get() >= getGoal().get()){
            challenge.done();
        }
    }
}
