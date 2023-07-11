package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.scripts.data.SceneTrigger;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public abstract class ChallengeTrigger {
    private final int paramIndex;
    private final AtomicInteger score = new AtomicInteger(0);
    private final int goal;

    public ChallengeTrigger (int paramIndex, int goal) {
        this.paramIndex = paramIndex;
        this.goal = goal;
    }
    /**
     * Trigger start action, sends PacketChallengeDataNotify
     */
    public void onBegin(WorldChallenge challenge){}
    /**
     * Trigger ends action
     */
    public void onFinish(WorldChallenge challenge){}
    /**
     * Trigger when monster died
     */
    public void onMonsterDeath(WorldChallenge challenge, EntityMonster monster){}
    /**
     * Trigger when gadget died
     */
    public void onGadgetDeath(WorldChallenge challenge, EntityGadget gadget){}
    /**
     * Trigger constantly to check if challenge progress exceeds time limit
     */
    public void onCheckTimeout(WorldChallenge challenge){}
    /**
     * Trigger when damaging gadget
     */
    public void onGadgetDamage(WorldChallenge challenge, EntityGadget gadget){}
    public void onGroupTrigger(WorldChallenge challenge, SceneTrigger trigger){}
    /**
     * Trigger when an elemental reaction occurred
     */
    public void onElementReaction(WorldChallenge challenge, GameEntity defender, ElementReactionType reactionType){}
    /**
     * Trigger when damaging monster or player's shield
     */
    public void onDamageMonsterOrShield(WorldChallenge challenge, float damage){}
    /**
     * Trigger when child challenge finishes or fails
     */
    public void onIncFailSuccScore(WorldChallenge challenge, boolean useSucc, int score){}
}
