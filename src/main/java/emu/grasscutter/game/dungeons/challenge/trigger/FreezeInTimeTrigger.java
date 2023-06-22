package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;
import lombok.Setter;

public class FreezeInTimeTrigger extends ChallengeTrigger{
    @Getter private final ElementReactionType reactionType = ElementReactionType.Freeze;
    @Getter private final int inTime;
    @Getter @Setter private int timerTaskId = -1;

    public FreezeInTimeTrigger(int paramIndex, int inTime) {
        super(paramIndex);
        this.inTime = inTime;
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getScore().get()));
    }

    public void startCountDown(WorldChallenge challenge) {
        setTimerTaskId(Grasscutter.getGameServer().getScheduler().scheduleDelayedTask(() -> {
            challenge.getScore().set(0);
            challenge.getScene().broadcastPacket(
                new PacketChallengeDataNotify(challenge, getParamIndex(), challenge.getScore().get()));
            setTimerTaskId(-1);
        }, getInTime()));
    }

    @Override
    public void onFreezeInTime(WorldChallenge challenge, EntityMonster monster, ElementReactionType reactionType) {
        int oldScore = challenge.getScore().get();
        int newScore = reactionType == getReactionType() ? challenge.increaseScore() : oldScore;
        if (oldScore != newScore) {
            challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), newScore));
        }

        if (getTimerTaskId() < 0) {
            startCountDown(challenge);
        }

        if (newScore >= challenge.getGoal()) {
            Grasscutter.getGameServer().getScheduler().cancelTask(getTimerTaskId());
            challenge.done();
        }
    }
}
