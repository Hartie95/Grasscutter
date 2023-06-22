package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.scripts.data.SceneTrigger;
import lombok.Getter;

public abstract class ChallengeTrigger {
    @Getter private final int paramIndex;

    public ChallengeTrigger (int paramIndex) {
        this.paramIndex = paramIndex;
    }
    public void onBegin(WorldChallenge challenge){}
    public void onFinish(WorldChallenge challenge){}
    public void onMonsterDeath(WorldChallenge challenge, EntityMonster monster){}
    public void onMonsterDamage(WorldChallenge challenge, EntityMonster monster, float damageAmount){}
    public void onGadgetDeath(WorldChallenge challenge, EntityGadget gadget){}
    public void onCheckTimeout(WorldChallenge challenge){}
    public void onGadgetDamage(WorldChallenge challenge, EntityGadget gadget){}
    public void onGroupTrigger(WorldChallenge challenge, SceneTrigger trigger){}
    public void onElementReactionCount(WorldChallenge challenge, EntityMonster monster, ElementReactionType reactionType){}
    public void onFreezeInTime(WorldChallenge challenge, EntityMonster monster, ElementReactionType reactionType){}
    public void onIncFailSuccScore(WorldChallenge challenge, int index, int score){}
}
