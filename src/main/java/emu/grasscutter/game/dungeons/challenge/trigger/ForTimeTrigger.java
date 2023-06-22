package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class ForTimeTrigger  extends ChallengeTrigger{

    public ForTimeTrigger(int paramIndex) {
        super(paramIndex);
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getStartedAt() + challenge.getTimeLimit()));
    }

    @Override
    public void onCheckTimeout(WorldChallenge challenge) {
        if(challenge.getScene().getSceneTimeSeconds() - challenge.getStartedAt() > challenge.getTimeLimit()){
            challenge.done();
        }
    }
}
