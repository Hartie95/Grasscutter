package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

@Getter
public class FatherTrigger extends ChallengeTrigger{
    public FatherTrigger () {
        super(0);
    }

    // param index is always 1 for successCount and 2 for failCount
    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, 1, 0));
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, 2, 0));
    }

    @Override
    public void onIncFailSuccScore(WorldChallenge challenge, int index, int score) {
        int newScore = index == 1 ? challenge.increaseScore(score) : challenge.incFailScore(score);
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, index, newScore));

        int condCount = index == 1 ? challenge.getSuccessCount() : challenge.getFailCount();
        if (newScore < condCount) return;

        if (index == 1) {
            challenge.done();
            return;
        }

        challenge.fail();
    }
}
