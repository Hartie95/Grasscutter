package emu.grasscutter.game.ability;

import java.util.HashMap;
import java.util.Map;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.binout.AbilityData;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.data.common.DynamicFloat;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.server.event.entity.EntityDamageEvent;
import lombok.Getter;

public class Ability {
    @Getter private AbilityData data;
    @Getter private GameEntity owner;

    @Getter private AbilityManager manager;

    @Getter private Map<String, AbilityModifierController> modifiers = new HashMap<>();

    @Getter private int hash;

    public Ability(AbilityData data, GameEntity owner) {
        this.data = data;
        this.owner = owner;
        this.manager = owner.getScene().getWorld().getHost().getAbilityManager();

        hash = AbilityHash(data.abilityName);

        data.initialize();
    }

    public void onAdded() {
        if(data.onAdded == null) return;

        for (AbilityModifierAction action : data.onAdded) {
            manager.executeAction(this, action);
        }
    }

    public void onRemoved() {
        Map<String, AbilityModifierController> tempModifiers = new HashMap<>(modifiers);
        tempModifiers.values().forEach(m -> m.onRemoved());
    }

    public void onBeingHit(EntityDamageEvent event) {
        Map<String, AbilityModifierController> tempModifiers = new HashMap<>(modifiers);
        tempModifiers.values().forEach(m -> m.onBeingHit(event));
    }

    public static int AbilityHash(String str)
    {
        long hash = 0;
        char[] asCharArray = str.toCharArray();
        for (int i = 0; i < str.length(); i++)
        {
            hash = ((asCharArray[i] + 131 * hash) & 0xFFFFFFFF);
        }
        return (int)hash;
    }
}
