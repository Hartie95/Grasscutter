package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

import java.util.List;

public class ElementReactionTrigger extends ChallengeTrigger{
    @Getter private final List<ElementReactionType> GOAL_REACTION_TYPE;

    public ElementReactionTrigger(int paramIndex, List<ElementReactionType> reactionType) {
        super(paramIndex);
        this.GOAL_REACTION_TYPE = reactionType;
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        if (getParamIndex() < 1) return; // when challenge is FreezeInTime

        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(
            challenge, getParamIndex(), challenge.getScore().get()));
    }

    @Override
    public void onElementReaction(WorldChallenge challenge, ElementReactionType reactionType) {
        if (!getGOAL_REACTION_TYPE().contains(reactionType)) return;

        int newScore = challenge.increaseScore();

        if (getParamIndex() > 0) { // when challenge is not FreezeInTime
            challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), newScore));
        }

        if (newScore >= challenge.getGoal()) {
            challenge.done();
        }
    }
}
