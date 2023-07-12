package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;

import java.util.List;

@Getter
public class ElementReactionTrigger extends ChallengeTrigger{
    private final List<ElementReactionType> GOAL_REACTION_TYPE;
    /**
     * Target type that invoke this trigger
     * i.e. Some challenges require player(AVATAR) avoids getting freeze for x number of times,
     * but mainly invoking elemental reaction on MONSTERS
     * */
    private final Class<? extends GameEntity> DEFENDER_TYPE;
    private final boolean RUN_SUCC;

    public ElementReactionTrigger(int paramIndex, int goal, ElementReactionType reactionType) {
        this(paramIndex, goal, List.of(reactionType), true);
    }

    public ElementReactionTrigger(int paramIndex, int goal, ElementReactionType reactionType, boolean shouldSucc) {
        this(paramIndex, goal, List.of(reactionType), shouldSucc);
    }

    /**
     * Used when challenge requires player to invoke certain(or multiple) elemental reaction
     * */
    public ElementReactionTrigger(int paramIndex, int goal, List<ElementReactionType> reactionType, boolean shouldSucc) {
        super(paramIndex, goal);
        this.GOAL_REACTION_TYPE = reactionType;
        this.DEFENDER_TYPE = shouldSucc ? EntityMonster.class : EntityAvatar.class;
        this.RUN_SUCC = shouldSucc;
    }

    @Override
    public void onElementReaction(WorldChallenge challenge, GameEntity defender, ElementReactionType reactionType) {
        if (!getGOAL_REACTION_TYPE().contains(reactionType) || defender.getClass() != getDEFENDER_TYPE()) return;

        int newScore = getScore().incrementAndGet();
        onBegin(challenge);

        if (newScore < getGoal().get()) return;

        if (isRUN_SUCC()) {
            challenge.done();
        } else {
            challenge.fail();
        }
    }
}
