package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

public class ElementReactionCountTrigger extends ChallengeTrigger{
    @Getter private final ElementReactionType goalReactionType;

    public ElementReactionCountTrigger(int paramIndex, int reactionType) {
        super(paramIndex);
        this.goalReactionType = ElementReactionType.getTypeByValue(reactionType);
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getScore().get()));
    }

    @Override
    public void onElementReactionCount(WorldChallenge challenge, EntityMonster monster, ElementReactionType reactionType) {
        int oldScore = challenge.getScore().get();
        int newScore = reactionType == getGoalReactionType() ? challenge.increaseScore() : oldScore;

        if (oldScore != newScore) {
            challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), newScore));
        }

        if (newScore >= challenge.getGoal()) {
            challenge.done();
        }
    }
}
