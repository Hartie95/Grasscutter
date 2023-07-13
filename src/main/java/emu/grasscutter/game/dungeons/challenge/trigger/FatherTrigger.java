package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class FatherTrigger extends ChallengeTrigger{
    private final AtomicInteger failScore = new AtomicInteger(0);

    /**
     * Used when challenge have multiple sub/child challenge
     * */
    public FatherTrigger () {
        super(0, 0);
    }

    // param index is always 1 for successCount and 2 for failCount
    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, 1, getScore().get()));
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, 2, getFailScore().get()));
    }

    /**
     * @param challenge should be a FATHER challenge
     * @param score should be score from CHILD challenge
     * */
    @Override
    public void onIncFailSuccScore(WorldChallenge challenge, boolean useSucc, int score) {
        // should add score from CHILD challenge and check against count from FATHER challenge
        int condCount = challenge.getScoreInfo().get(useSucc);
        int newScore = useSucc ? getScore().addAndGet(score) : getFailScore().addAndGet(score);
        onBegin(challenge);
        if (newScore < condCount) return;

        if (useSucc) {
            challenge.done();
        } else {
            challenge.fail();
        }
    }
}
