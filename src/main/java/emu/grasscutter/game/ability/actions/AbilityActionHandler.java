package emu.grasscutter.game.ability.actions;

import emu.grasscutter.Loggers;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.game.entity.GameEntity;
import org.slf4j.Logger;

public abstract class AbilityActionHandler {
    public static final Logger logger = Loggers.getAbilitySystem();

    public abstract boolean execute(Ability ability, AbilityModifierAction action, byte[] abilityData, GameEntity<?> target);

}
