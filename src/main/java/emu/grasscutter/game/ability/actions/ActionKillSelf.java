package emu.grasscutter.game.ability.actions;

import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.game.entity.GameEntity;

@AbilityAction(AbilityModifierAction.Type.KillSelf)
public class ActionKillSelf extends AbilityActionHandler {
    @Override
    public boolean execute(Ability ability, AbilityModifierAction action, byte[] abilityData, GameEntity<?> target) {
        //KillSelf should not have a target field, so target it's the actual entity to be applied, TODO: Check if this is always true
        if(target == null) {
            logger.warn("Tried killing null target");
            return false;
        }

        target.getScene().killEntity(target);

        return true;
    }
}
