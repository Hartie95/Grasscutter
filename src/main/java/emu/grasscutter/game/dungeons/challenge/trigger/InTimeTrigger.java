package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class InTimeTrigger extends ChallengeTrigger{
    @Override
    public void onBegin(WorldChallenge challenge) {
        int paramIndex = challenge.getParamList().indexOf(challenge.getTimeLimit());

        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, 
            paramIndex+1, 
            challenge.getTimeLimit() * 4));
    }

    @Override
    public void onCheckTimeout(WorldChallenge challenge) {
        var current = System.currentTimeMillis();
        if(current - challenge.getStartedAt() > challenge.getTimeLimit() * 1000L){
            challenge.fail();
        }
    }
}
