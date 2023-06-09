package emu.grasscutter.game.ability;

import java.util.Map;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;

public class AbilityLocalIdGenerator {
    public enum ConfigAbilitySubContainerType
    {
        NONE(0),
        ACTION(1),
        MIXIN(2),
        MODIFIER_ACTION(3),
        MODIFIER_MIXIN(4);

        public long value;
        private ConfigAbilitySubContainerType(long value) {
            this.value = value;
        }
    }

    public ConfigAbilitySubContainerType type = ConfigAbilitySubContainerType.NONE;
    public long ModifierIndex = 0;
    public long ConfigIndex = 0;
    public long MixinIndex = 0;
    private long ActionIndex = 0;

    public AbilityLocalIdGenerator(ConfigAbilitySubContainerType type)
    {
        this.type = type;
    }

    public void InitializeActionLocalIds(AbilityModifierAction actions[], Map<Integer, AbilityModifierAction> localIdToAction)
    {
        if (actions == null) return;
        ActionIndex = 0;
        for (int i = 0; i < actions.length; i++)
        {
            ActionIndex++;
            long id = GetLocalId();
            localIdToAction.put((int)id, actions[i]);
        }
        ActionIndex = 0;
    }

    public long GetLocalId()
    {
        switch (type)
        {
            case ACTION:
                return (long)type.value + (ConfigIndex << 3) + (ActionIndex << 9);
            case MIXIN:
                return (long)type.value + (MixinIndex << 3) + (ConfigIndex << 9) + (ActionIndex << 15);
            case MODIFIER_ACTION:
                return (long)type.value + (ModifierIndex << 3) + (ConfigIndex << 9) + (ActionIndex << 15);
            case MODIFIER_MIXIN:
                return (long)type.value + (ModifierIndex << 3) + (MixinIndex << 9) + (ConfigIndex << 15) + (ActionIndex << 21);
            case NONE:
                Grasscutter.getLogger().error("Ability local id generator using NONE type.");
                break;
        }

        return -1;
    }
}
