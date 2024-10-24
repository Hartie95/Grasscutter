package emu.grasscutter.game.ability.actions;

import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.game.entity.EntityClientGadget;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.GameEntity;

@AbilityAction(AbilityModifierAction.Type.SetGlobalValueToOverrideMap)
public class ActionSetGlobalValueToOverrideMap extends AbilityActionHandler {
    @Override
    public boolean execute(Ability ability, AbilityModifierAction action, byte[] abilityData, GameEntity<?> target) {
        //TODO:
        var entity = target;
        if(action.isFromOwner) {
            if(target instanceof EntityClientGadget gadget)
                entity = entity.getScene().getEntityById(gadget.getOwnerEntityId());
            else if(target instanceof EntityGadget gadget)
                entity = gadget.getOwnerEntity();
        }

        var globalValueKey = action.globalValueKey;
        var abilityFormula = action.abilityFormula;

        if(!entity.getGlobalAbilityValues().containsKey(globalValueKey)) {
            logger.debug("Action does not contains {} global key", globalValueKey);
            return true;
        }

        var globalValue = entity.getGlobalAbilityValues().getOrDefault(globalValueKey, 0.0f);
        if(abilityFormula.compareTo("DummyThrowSpeed") == 0) {
            globalValue = ((globalValue * 30.0f) / ((float)Math.sin(0.9424778) * 100.0f)) - 1.0f;
        }

        entity.getGlobalAbilityValues().put(globalValueKey, globalValue); //Reserahc if this is needed
        ability.getAbilitySpecials().put(action.overrideMapKey, globalValue.floatValue()); //Override our own

        //TODO: ChangeServerGlobalValueNotify

        return true;
    }
}
