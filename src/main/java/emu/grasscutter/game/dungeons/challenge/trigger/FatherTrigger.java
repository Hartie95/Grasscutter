package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.ChildChallenge;
import emu.grasscutter.game.dungeons.challenge.FatherChallenge;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class FatherTrigger extends ChallengeTrigger{
    private final int successCount;
    private final int failCount;

    public FatherTrigger (int successCount, int failCount) {
        super(0);
        this.successCount = successCount;
        this.failCount = failCount;
    }

    // param index for successCount is always 1 and 2 for failCount
    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, 1, 0));
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, 2, 0));
    }

    @Override
    public void onIncFailSuccScore(WorldChallenge challenge, int index, int score) {
        if (! (challenge instanceof FatherChallenge fatherChallenge)) return;

        int newScore = index == 1 ? fatherChallenge.incSuccessScore(score) : fatherChallenge.incFailScore(score);
        challenge.getScene().broadcastPacket(
            new PacketChallengeDataNotify(challenge, index, newScore));

        int condCount = index == 1 ? getSuccessCount() : getFailCount();
        if (newScore < condCount) return;

        if (index == 1) {
            fatherChallenge.done();
            return;
        }

        fatherChallenge.fail();
    }
}
