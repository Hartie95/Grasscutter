package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class KillMonsterCountTrigger extends ChallengeTrigger{
    public KillMonsterCountTrigger(int paramIndex){
        super(paramIndex);
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), 0));
    }

    @Override
    public void onMonsterDeath(WorldChallenge challenge, EntityMonster monster) {
        int newScore = challenge.increaseScore();
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), newScore));

        if(newScore >= challenge.getGoal()){
            challenge.done();
        }

    }
}
