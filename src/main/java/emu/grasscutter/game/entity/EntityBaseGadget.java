package emu.grasscutter.game.entity;

import emu.grasscutter.data.binout.ConfigGadget;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.quest.enums.QuestTrigger;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.ScriptArgs;

import static emu.grasscutter.scripts.constants.EventType.EVENT_SPECIFIC_GADGET_HP_CHANGE;

public abstract class EntityBaseGadget extends GameEntity {

    public EntityBaseGadget(Scene scene) {
        super(scene);
    }

    public abstract int getGadgetId();

    @Override
    public void onDeath(int killerId) {
        super.onDeath(killerId); // Invoke super class's onDeath() method.

        getScene().getPlayers().forEach(p -> p.getQuestManager().queueEvent(QuestTrigger.QUEST_CONTENT_DESTROY_GADGET, this.getGadgetId()));
    }

    @Override
    public void damage(float amount, int killerId) {
        super.damage(amount, killerId);

        if(this.getFightProperties() == null){
            return;
        }
        float hpAfterDamage = this.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);
        // param2 unused for now
        getScene().getScriptManager().callEvent(EVENT_SPECIFIC_GADGET_HP_CHANGE, new ScriptArgs(getConfigId(), getGadgetId())
            .setSourceEntityId(getId())
            .setParam3((int)hpAfterDamage)
            .setEventSource(Integer.toString(getConfigId())));
    }

    protected void fillFightProps(ConfigGadget configGadget) {
        if (configGadget == null || configGadget.getCombat() == null) {
            return;
        }
        var combatData = configGadget.getCombat();
        var combatProperties = combatData.getProperty();

        var targetHp = combatProperties.getHP();
        setFightProperty(FightProperty.FIGHT_PROP_MAX_HP, targetHp);
        setFightProperty(FightProperty.FIGHT_PROP_BASE_HP, targetHp);
        if (combatProperties.isInvincible()) {
            targetHp = Float.POSITIVE_INFINITY;
        }
        setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, targetHp);

        var atk = combatProperties.getAttack();
        setFightProperty(FightProperty.FIGHT_PROP_BASE_ATTACK, atk);
        setFightProperty(FightProperty.FIGHT_PROP_CUR_ATTACK, atk);

        var def = combatProperties.getDefence();
        setFightProperty(FightProperty.FIGHT_PROP_BASE_DEFENSE, def);
        setFightProperty(FightProperty.FIGHT_PROP_CUR_DEFENSE, def);

        setLockHP(combatProperties.isLockHP());
    }
}
