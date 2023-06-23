package emu.grasscutter.game.ability;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.props.ElementReactionType;
import emu.grasscutter.net.proto.AbilityMetaTriggerElementReactionOuterClass.AbilityMetaTriggerElementReaction;
import lombok.val;
import org.reflections.Reflections;

import com.google.protobuf.InvalidProtocolBufferException;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.AbilityData;
import emu.grasscutter.data.binout.AbilityModifierEntry;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.entity.gadget.GadgetGatherObject;
import emu.grasscutter.game.player.BasePlayerManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.net.proto.AbilityInvokeEntryHeadOuterClass.AbilityInvokeEntryHead;
import emu.grasscutter.net.proto.AbilityInvokeEntryOuterClass.AbilityInvokeEntry;
import emu.grasscutter.net.proto.AbilityMetaModifierChangeOuterClass.AbilityMetaModifierChange;
import emu.grasscutter.net.proto.AbilityMetaModifierDurabilityChangeOuterClass.AbilityMetaModifierDurabilityChange;
import emu.grasscutter.net.proto.AbilityMetaReInitOverrideMapOuterClass.AbilityMetaReInitOverrideMap;
import emu.grasscutter.net.proto.AbilityMixinCostStaminaOuterClass.AbilityMixinCostStamina;
import emu.grasscutter.net.proto.AbilityScalarValueEntryOuterClass.AbilityScalarValueEntry;
import emu.grasscutter.net.proto.ModifierActionOuterClass.ModifierAction;
import io.netty.util.concurrent.FastThreadLocalThread;
import lombok.Getter;
import emu.grasscutter.net.proto.AbilityMetaAddAbilityOuterClass.AbilityMetaAddAbility;

public final class AbilityManager extends BasePlayerManager {
    HealAbilityManager healAbilityManager;

    private final HashMap<AbilityModifierAction.Type, AbilityActionHandler> actionHandlers;

    public static final ExecutorService eventExecutor;
    static {
        eventExecutor = new ThreadPoolExecutor(4, 4,
            60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000),
            FastThreadLocalThread::new, new ThreadPoolExecutor.AbortPolicy());
    }

    @Getter private boolean abilityInvulnerable = false;

    public AbilityManager(Player player) {
        super(player);
        this.healAbilityManager = new HealAbilityManager(player);

        this.actionHandlers = new HashMap<>();

        this.registerHandlers();
    }

    public void registerHandlers() {
        Reflections reflections = new Reflections("emu.grasscutter.game.ability.actions");
        var handlerClasses = reflections.getSubTypesOf(AbilityActionHandler.class);

        for (var obj : handlerClasses) {
            try {
                if (obj.isAnnotationPresent(AbilityAction.class)) {
                    AbilityModifierAction.Type abilityAction = obj.getAnnotation(AbilityAction.class).value();
                    actionHandlers.put(abilityAction, obj.getDeclaredConstructor().newInstance());
                } else {
                    return;
                }
            } catch (Exception e) {
                Grasscutter.getLogger().error("Unable to register handler.", e);
            }
        }
    }

    public void executeAction(Ability ability, AbilityModifierAction action) {
        AbilityActionHandler handler = actionHandlers.get(action.type);

        if (handler == null || ability == null) {
            Grasscutter.getLogger().debug("Could not execute ability action {} at {}", action.type, ability);
            return;
        }

        eventExecutor.submit(() -> {
            if (!handler.execute(ability, action)) {
                Grasscutter.getLogger().debug("exec ability action failed {} at {}", action.type, ability);
            }
        });
    }

    public void onAbilityInvoke(AbilityInvokeEntry invoke) throws Exception {
        this.healAbilityManager.healHandler(invoke);

         //Grasscutter.getLogger().info(invoke.getArgumentType() + " (" + invoke.getArgumentTypeValue() + "): " + Utils.bytesToHex(invoke.toByteArray()));
        if(invoke.getEntityId() == 67109298) {
            Grasscutter.getLogger().info(invoke.getArgumentType() + " (" + invoke.getArgumentTypeValue() + "): " + invoke.getEntityId());
        }
        switch (invoke.getArgumentType()) {
            case ABILITY_INVOKE_ARGUMENT_META_OVERRIDE_PARAM -> this.handleOverrideParam(invoke);
            case ABILITY_INVOKE_ARGUMENT_META_REINIT_OVERRIDEMAP -> this.handleReinitOverrideMap(invoke);
            case ABILITY_INVOKE_ARGUMENT_META_MODIFIER_CHANGE -> this.handleModifierChange(invoke);
            case ABILITY_INVOKE_ARGUMENT_MIXIN_COST_STAMINA -> this.handleMixinCostStamina(invoke);
            case ABILITY_INVOKE_ARGUMENT_ACTION_GENERATE_ELEM_BALL -> this.handleGenerateElemBall(invoke);
            case ABILITY_INVOKE_ARGUMENT_META_GLOBAL_FLOAT_VALUE -> this.handleGlobalFloatValue(invoke);
            case ABILITY_INVOKE_ARGUMENT_META_MODIFIER_DURABILITY_CHANGE -> this.handleModifierDurabilityChange(invoke);
            case ABILITY_INVOKE_ARGUMENT_META_ADD_NEW_ABILITY -> this.handleAddNewAbility(invoke);
            case ABILITY_INVOKE_ARGUMENT_NONE -> this.handleInvoke(invoke);
            case ABILITY_INVOKE_ARGUMENT_META_TRIGGER_ELEMENT_REACTION -> this.handleTriggerElementReaction(invoke);
            default -> {}
        }
    }

    /**
     * Invoked when a player starts a skill.
     * @param player The player who started the skill.
     * @param skillId The skill ID.
     * @param casterId The caster ID.
     */
    public void onSkillStart(Player player, int skillId, int casterId) {
        // Check if the player matches this player.
        if (player.getUid() != this.player.getUid()) {
            return;
        }

        // Check if the caster matches the player.
        if (player.getTeamManager().getCurrentAvatarEntity().getId() != casterId) {
            return;
        }

        var skillData = GameData.getAvatarSkillDataMap().get(skillId);
        if (skillData == null) {
            return;
        }

        // Check if the skill is an elemental burst.
        if (skillData.getCostElemVal() <= 0) {
            return;
        }

        // Set the player as invulnerable.
        this.abilityInvulnerable = true;
    }

    /**
     * Invoked when a player ends a skill.
     * @param player The player who started the skill.
     */
    public void onSkillEnd(Player player) {
        // Check if the player matches this player.
        if (player.getUid() != this.player.getUid()) {
            return;
        }

        // Check if the player is invulnerable.
        if (!this.abilityInvulnerable) {
            return;
        }

        // Set the player as not invulnerable.
        this.abilityInvulnerable = false;
    }

    private boolean checkAbility(Ability ability, int hash, GameEntity entity, int instancedAbilityId){
        val abilityName = ability.getData().abilityName;
        val entityInstanceName = entity.getInstanceToName().get(instancedAbilityId);
        return ability.getHash() == hash || abilityName != null && abilityName.equals(entityInstanceName);
    }

    private void handleInvoke(AbilityInvokeEntry invoke) {
        GameEntity entity = this.player.getScene().getEntityById(invoke.getEntityId());
        if (entity == null) {
            return;
        }

        AbilityInvokeEntryHead head = invoke.getHead();

        Grasscutter.getLogger().debug("{} {} {}", head.getInstancedAbilityId(), entity.getInstanceToHash(), head.getLocalId());

        Integer hash = entity.getInstanceToHash().get(head.getInstancedAbilityId());
        if(hash == null) {
            var abilities = entity.getAbilities().values().toArray(new Ability[0]);

            if(head.getInstancedAbilityId() <= abilities.length) {
                var ability = abilities[head.getInstancedAbilityId() - 1];
                Grasscutter.getLogger().debug("-> {}", ability.getData().localIdToAction);
                AbilityModifierAction action = ability.getData().localIdToAction.get(head.getLocalId());
                if(action != null) ability.getManager().executeAction(ability, action);
            }

            return;
        }

        entity.getAbilities().values().stream()
            .filter(a -> checkAbility(a, hash, entity, head.getInstancedAbilityId()))
            .forEach(ability -> {
                AbilityModifierAction action = ability.getData().localIdToAction.get(head.getLocalId());
                if (action != null) ability.getManager().executeAction(ability, action);
            });
    }

    private void handleOverrideParam(AbilityInvokeEntry invoke) throws Exception {
        GameEntity entity = this.player.getScene().getEntityById(invoke.getEntityId());

        if (entity == null) {
            return;
        }

        AbilityScalarValueEntry entry = AbilityScalarValueEntry.parseFrom(invoke.getAbilityData());

        entity.getMetaOverrideMap().put(entry.getKey().getStr(), entry.getFloatValue());
    }

    private void handleReinitOverrideMap(AbilityInvokeEntry invoke) throws Exception {
        GameEntity entity = this.player.getScene().getEntityById(invoke.getEntityId());

        if (entity == null) {
            return;
        }

        AbilityMetaReInitOverrideMap map = AbilityMetaReInitOverrideMap.parseFrom(invoke.getAbilityData());

        for (AbilityScalarValueEntry entry : map.getOverrideMapList()) {
            entity.getMetaOverrideMap().put(entry.getKey().getStr(), entry.getFloatValue());
        }
    }

    private void handleModifierChange(AbilityInvokeEntry invoke) throws Exception {
        // Sanity checks
        GameEntity target = this.player.getScene().getEntityById(invoke.getEntityId());
        if (target == null) {
            return;
        }

        AbilityMetaModifierChange data = AbilityMetaModifierChange.parseFrom(invoke.getAbilityData());
        if (data == null) {
            return;
        }

        // Destroying rocks
        if (target instanceof EntityGadget targetGadget && targetGadget.getContent() instanceof GadgetGatherObject gatherObject) {
            if (data.getAction() == ModifierAction.REMOVED) {
                gatherObject.dropItems(this.getPlayer());
                return;
            }
        }

        // Sanity checks
        AbilityInvokeEntryHead head = invoke.getHead();

        if(data.getAction() == ModifierAction.REMOVED) {
            Ability ability = target.getAbilities().get(data.getParentAbilityName().getStr());
            if(ability != null) {
                AbilityModifierController modifier = ability.getModifiers().get(head.getInstancedModifierId());
                if(modifier != null) {
                    modifier.onRemoved();
                    ability.getModifiers().remove(modifier);
                }
            }
        }

        if(data.getAction() == ModifierAction.ADDED) {
            String modifierString = data.getParentAbilityName().getStr();

            Integer hash = target.getInstanceToHash().get(head.getInstancedAbilityId());
            if (hash == null) return;
            target.getAbilities().values().stream()
                .filter(a -> checkAbility(a, hash, target, head.getInstancedAbilityId()))
                .map(a -> a.getModifiers().get(modifierString))
                .filter(Objects::nonNull)
                .forEach(a -> a.setLocalId(head.getInstancedModifierId()));
        }

        GameEntity sourceEntity = this.player.getScene().getEntityById(data.getApplyEntityId());
        if (sourceEntity == null) {
            return;
        }

        // This is not how it works, but we will keep it for now since healing abilities don't work properly anyway
        if (data.getAction() == ModifierAction.ADDED) {
            // Handle add modifier here
            String modifierString = data.getParentAbilityName().getStr();
            AbilityModifierEntry modifier = GameData.getAbilityModifiers().get(modifierString);

            if (modifier != null && modifier.getOnAdded().size() > 0) {
                for (AbilityModifierAction action : modifier.getOnAdded()) {
                    this.invokeAction(action, target, sourceEntity);
                }
            }

            // Add to meta modifier list
            target.getMetaModifiers().put(head.getInstancedModifierId(), modifierString);
        } else if (data.getAction() == ModifierAction.REMOVED) {
            // Handle remove modifier
            String modifierString = target.getMetaModifiers().get(head.getInstancedModifierId());

            if (modifierString != null) {
                // Get modifier and call on remove event
                AbilityModifierEntry modifier = GameData.getAbilityModifiers().get(modifierString);

                if (modifier != null && modifier.getOnRemoved().size() > 0) {
                    for (AbilityModifierAction action : modifier.getOnRemoved()) {
                        this.invokeAction(action, target, sourceEntity);
                    }
                }

                // Remove from meta modifiers
                target.getMetaModifiers().remove(head.getInstancedModifierId());
            }
        }
    }

    private void handleMixinCostStamina(AbilityInvokeEntry invoke) throws InvalidProtocolBufferException {
        AbilityMixinCostStamina costStamina = AbilityMixinCostStamina.parseFrom((invoke.getAbilityData()));
        this.getPlayer().getStaminaManager().handleMixinCostStamina(costStamina.getIsSwim());
    }

    private void handleGenerateElemBall(AbilityInvokeEntry invoke) throws InvalidProtocolBufferException {
        this.player.getEnergyManager().handleGenerateElemBall(invoke);
    }

    private void handleGlobalFloatValue(AbilityInvokeEntry invoke) throws InvalidProtocolBufferException {
        AbilityScalarValueEntry entry = AbilityScalarValueEntry.parseFrom(invoke.getAbilityData());
        if(entry.getKey().hasStr() && entry.getKey().getStr().equals("_ABILITY_UziExplode_Count") && entry.hasFloatValue() && entry.getFloatValue() == 2.0f){
            player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_SKILL, 10006);
        }
    }

    private void invokeAction(AbilityModifierAction action, GameEntity target, GameEntity sourceEntity) {
        switch (action.type) {
            case HealHP -> {}
            case LoseHP -> {
                if (action.amountByTargetCurrentHPRatio == null) {
                    return;
                }

                float damageAmount = action.amount.get();

                // if (action.amount.isDynamic && action.amount.dynamicKey != null) {
                //     damageAmount = sourceEntity.getMetaOverrideMap().getOrDefault(action.amount.dynamicKey, 0f);
                // }

                if (damageAmount > 0) {
                    target.damage(damageAmount);
                }
            }
            default -> {}
        }
    }

    private void handleModifierDurabilityChange(AbilityInvokeEntry invoke) throws InvalidProtocolBufferException {
        GameEntity target = this.player.getScene().getEntityById(invoke.getEntityId());
        if (target == null) {
            return;
        }

        AbilityMetaModifierDurabilityChange data = AbilityMetaModifierDurabilityChange.parseFrom(invoke.getAbilityData());
        if (data == null) {
            return;
        }

        AbilityInvokeEntryHead head = invoke.getHead();

        Integer hash = target.getInstanceToHash().get(head.getInstancedAbilityId());
        if(hash == null) return;
        target.getAbilities().values().stream()
            .filter(a -> checkAbility(a, hash, target,head.getInstancedAbilityId()))
            .flatMap(a -> a.getModifiers().values().stream())
            .filter(m -> m.getLocalId() == head.getInstancedModifierId())
            .forEach(modifier -> modifier.setElementDurability(data.getRemainDurability()));
    }

    private void handleAddNewAbility(AbilityInvokeEntry invoke) throws InvalidProtocolBufferException {
        AbilityMetaAddAbility data = AbilityMetaAddAbility.parseFrom(invoke.getAbilityData());
        if (data == null) {
            return;
        }

        if(data.getAbility().getAbilityName().getHash() != 0) Grasscutter.getLogger().debug("Instancing {} in to {}", data.getAbility().getAbilityName().getHash(), data.getAbility().getInstancedAbilityId());
        else Grasscutter.getLogger().debug("Instancing {} in to {}", data.getAbility().getAbilityName().getStr(), data.getAbility().getInstancedAbilityId());

        GameEntity target = this.player.getScene().getEntityById(invoke.getEntityId());
        if (target == null) {
            return;
        }

        target.getInstanceToHash().put(data.getAbility().getInstancedAbilityId(), data.getAbility().getAbilityName().getHash());
        target.getInstanceToName().put(data.getAbility().getInstancedAbilityId(), data.getAbility().getAbilityName().getStr());
    }

    /**
     * Invoked when an entity triggered an elemental reaction.
     * @param invoke Holds information of elemental reaction, attacker and target.
     */
    private void handleTriggerElementReaction(AbilityInvokeEntry invoke) throws InvalidProtocolBufferException {
        if (getPlayer().getScene() == null) return;

        AbilityMetaTriggerElementReaction data = AbilityMetaTriggerElementReaction.parseFrom(invoke.getAbilityData());
        GameEntity attackEntity = getPlayer().getScene().getEntityById(data.getTriggerEntityId());
        GameEntity targetEntity = getPlayer().getScene().getEntityById(invoke.getEntityId());
        if (attackEntity == null || targetEntity == null) return;

        switch (attackEntity.getClass().getSimpleName()) {
            // client gadget can be Kaeya's ult crystals, Xiangling's ult and Ningguang's auto attack amber
            case "EntityAvatar", "EntityClientGadget" -> {
                ElementReactionType reactionType = ElementReactionType.getTypeByValue(data.getElementReactionType());
                Grasscutter.getLogger().debug(
                    "EntityAttack: {}, EntityId: {}", attackEntity, data.getTriggerEntityId());
                Grasscutter.getLogger().debug(
                    "EntityTarget: {}, EntityId: {}", targetEntity, invoke.getEntityId());
                Grasscutter.getLogger().debug(
                    "ReactionType: {}, Value: {}", reactionType, data.getElementReactionType());

                // Challenge related action
                Optional.ofNullable(getPlayer().getScene().getChallenge())
                    .ifPresent(sceneChallenge -> {
                        if (!(targetEntity instanceof EntityMonster entityMonster)) return;

                        sceneChallenge.onElementReaction(entityMonster, reactionType);
                    });
            }
            case "EntityMonster", "EntityGadget" -> {} // TODO
        }
    }

    public void addAbilityToEntity(GameEntity entity, String name) {
        Optional.ofNullable(GameData.getAbilityData(name))
            .ifPresent(data -> addAbilityToEntity(entity, data, name));
    }

    public void addAbilityToEntity(GameEntity entity, AbilityData abilityData, String id) {
        Ability ability = new Ability(abilityData, entity);
        entity.getAbilities().put(id, ability);

        ability.onAdded();
    }
}

