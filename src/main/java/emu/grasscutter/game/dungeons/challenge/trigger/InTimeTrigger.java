package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;

public class InTimeTrigger extends ChallengeTrigger{
    public InTimeTrigger(int paramIndex){
        super(paramIndex);
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        if (getParamIndex() < 1) return; // when challenge is FreezeInTime

        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getStartedAt() + challenge.getTimeLimit()));
    }

    @Override
    public void onCheckTimeout(WorldChallenge challenge) {
        if(challenge.getScene().getSceneTimeSeconds() - challenge.getStartedAt() <= challenge.getTimeLimit()) return;

        if (getParamIndex() < 1) {// when challenge is FreezeInTime
            challenge.getScore().set(0);
            challenge.setStartedAt(challenge.getScene().getSceneTimeSeconds());
            return;
        }
        challenge.fail();
    }
}
