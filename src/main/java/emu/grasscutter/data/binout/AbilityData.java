package emu.grasscutter.data.binout;

import java.util.HashMap;
import java.util.Map;

import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.AbilityLocalIdGenerator;
import emu.grasscutter.game.ability.AbilityLocalIdGenerator.ConfigAbilitySubContainerType;

public class AbilityData {
    public String abilityName;
    public Map<String, AbilityModifier> modifiers;
    public boolean isDynamicAbility;
    public Map<String, Float> abilitySpecials;
    public AbilityModifierAction[] onAdded;
    // abilityMixins
    // onAbilityStart
    // onKill

    public final Map<Integer, AbilityModifierAction> localIdToAction = new HashMap<>();

    private boolean _initialized = false;
    public void initialize() {
        if(_initialized) return;
        _initialized = true;

        if(modifiers == null) return;

        AbilityModifier _modifiers[] = modifiers.values().toArray(new AbilityModifier[0]);
        long modifierIndex = 0;
        for(int i = 0; i < _modifiers.length; i++) {
            long configIndex = 0L;
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onAdded, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onRemoved, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onBeingHit, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onAttackLanded, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onHittingOther, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onThinkInterval, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onKill, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onCrash, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onAvatarIn, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onAvatarOut, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onReconnect, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onChangeAuthority, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onVehicleIn, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onVehicleOut, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onZoneEnter, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onZoneExit, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onHeal, localIdToAction);
            initializeActionSubCategory(modifierIndex, configIndex++, _modifiers[i].onBeingHealed, localIdToAction);

            modifierIndex++;
        }
    }

    private void initializeActionSubCategory(long modifierIndex, long configIndex, AbilityModifierAction[] actions, Map<Integer, AbilityModifierAction> localIdToAction) {
        if(actions == null) return;

        AbilityLocalIdGenerator generator = new AbilityLocalIdGenerator(ConfigAbilitySubContainerType.MODIFIER_ACTION);
        generator.ModifierIndex = modifierIndex;
        generator.ConfigIndex = configIndex;

        generator.InitializeActionLocalIds(actions, localIdToAction);
    }
}
