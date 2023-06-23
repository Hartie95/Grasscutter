package emu.grasscutter.game.entity;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.MonsterCurveData;
import emu.grasscutter.data.excels.MonsterData;
import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.*;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.game.world.SceneGroupInstance;
import emu.grasscutter.net.proto.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import emu.grasscutter.net.proto.AnimatorParameterValueInfoPairOuterClass.AnimatorParameterValueInfoPair;
import emu.grasscutter.net.proto.EntityAuthorityInfoOuterClass.EntityAuthorityInfo;
import emu.grasscutter.net.proto.EntityClientDataOuterClass.EntityClientData;
import emu.grasscutter.net.proto.EntityRendererChangedInfoOuterClass.EntityRendererChangedInfo;
import emu.grasscutter.net.proto.GadgetInteractReqOuterClass.GadgetInteractReq;
import emu.grasscutter.net.proto.MonsterBornTypeOuterClass.MonsterBornType;
import emu.grasscutter.net.proto.PropPairOuterClass.PropPair;
import emu.grasscutter.net.proto.ProtEntityTypeOuterClass.ProtEntityType;
import emu.grasscutter.net.proto.SceneEntityAiInfoOuterClass.SceneEntityAiInfo;
import emu.grasscutter.net.proto.SceneEntityInfoOuterClass.SceneEntityInfo;
import emu.grasscutter.net.proto.SceneMonsterInfoOuterClass.SceneMonsterInfo;
import emu.grasscutter.net.proto.SceneWeaponInfoOuterClass.SceneWeaponInfo;
import emu.grasscutter.scripts.SceneScriptManager;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneMonster;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.event.entity.EntityDamageEvent;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.ProtoHelper;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

import static emu.grasscutter.scripts.constants.EventType.EVENT_SPECIFIC_MONSTER_HP_CHANGE;

public class EntityMonster extends GameEntity {
    @Getter(onMethod = @__(@Override))
    private final Int2FloatOpenHashMap fightProperties;

    @Getter(onMethod = @__(@Override))
    private final Position position;
    @Getter(onMethod = @__(@Override))
    private final Position rotation;
    @Getter private final MonsterData monsterData;
    @Getter private final Position bornPos;
    @Getter private final int level;
    @Getter private int weaponEntityId;
    @Getter @Setter private int poseId;
    @Getter @Setter private int aiId = -1;

    @Getter @Setter private SceneMonster metaMonster;

    public EntityMonster(Scene scene, MonsterData monsterData, Position pos, int level) {
        super(scene);
        this.id = getWorld().getNextEntityId(EntityIdType.MONSTER);
        this.monsterData = monsterData;
        this.fightProperties = new Int2FloatOpenHashMap();
        this.position = new Position(pos);
        this.rotation = new Position();
        this.bornPos = getPosition().clone();
        this.level = level;

        // Monster weapon
        if (getMonsterWeaponId() > 0) {
            this.weaponEntityId = getWorld().getNextEntityId(EntityIdType.WEAPON);
        }

        this.recalcStats();
    }

    @Override
    public int getEntityTypeId() {
        return getMonsterId();
    }

    public int getMonsterWeaponId() {
        return getMonsterData().getWeaponId();
    }

    private int getMonsterId() {
        return getMonsterData().getId();
    }

    @Override
    public boolean isAlive() {
        return getFightProperty(FightProperty.FIGHT_PROP_CUR_HP) > 0f;
    }

    @Override
    public void onInteract(Player player, GadgetInteractReq interactReq) {
        Optional.ofNullable(GameData.getEnvAnimalGatherConfigDataMap().get(getMonsterData().getId()))
            .ifPresent(gatherData -> {
                player.getInventory().addItem(gatherData.getGatherItem(), ActionReason.SubfieldDrop);
                getScene().killEntity(this);
            });
    }

    @Override
    public void onCreate() {
        // Lua event
        getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_ANY_MONSTER_LIVE, getConfigId()));
    }

    @Override
    public void damage(float amount, int killerId, ElementType attackType) {
        // Get HP before damage.
        float hpBeforeDamage = getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);

        // Apply damage.
        super.damage(amount, killerId, attackType);

        // Get HP after damage.
        float hpAfterDamage = getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);

        // Invoke energy drop logic.
        getScene().getPlayers().forEach(p ->
            p.getEnergyManager().handleMonsterEnergyDrop(this, hpBeforeDamage, hpAfterDamage));

        // invoke challenge trigger if any
        Scene scene = getScene();
        // first set the challenge data
        Optional.ofNullable(scene.getChallenge()).ifPresent(c -> c.onDamageMonsterOrShield(this, amount));
    }

    @Override
    public void callLuaHPEvent(EntityDamageEvent event) {
        super.callLuaHPEvent(event);
        getScene().getScriptManager()
            .callEvent(new ScriptArgs(getGroupId(), EVENT_SPECIFIC_MONSTER_HP_CHANGE, getConfigId(), getMonsterData().getId())
            .setSourceEntityId(getId())
            .setParam3((int) getFightProperty(FightProperty.FIGHT_PROP_CUR_HP))
            .setEventSource(Integer.toString(getConfigId())));
    }

    @Override
    public void onDeath(int killerId) {
        super.onDeath(killerId); // Invoke super class's onDeath() method.
        Scene scene = getScene();
        if (scene == null) return;

        Optional.ofNullable(getSpawnEntry()).ifPresent(scene.getDeadSpawnedEntities()::add);

        // first set the challenge data
        Optional.ofNullable(scene.getChallenge()).ifPresent(c -> c.onMonsterDeath(this));

        SceneScriptManager scriptManager = scene.getScriptManager();
        if (scriptManager.isInit() && getGroupId() > 0) {
            Optional.ofNullable(scriptManager.getScriptMonsterSpawnService()).ifPresent(s -> s.onMonsterDead(this));

            // prevent spawn monster after success
            /*if (challenge.map(c -> c.inProgress()).orElse(true) || getScene().getChallenge() == null) {
                scriptManager.callEvent(new ScriptArgs(EventType.EVENT_ANY_MONSTER_DIE, this.getConfigId()));
            }*/
            scriptManager.callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_ANY_MONSTER_DIE, getConfigId()));
        }
        // Battle Pass and Quest trigger
        scene.getPlayers().forEach(p -> {
            p.getBattlePassManager().triggerMission(WatcherTriggerType.TRIGGER_MONSTER_DIE, getMonsterId(), 1);
            p.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_MONSTER_DIE, getMonsterId());
            p.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_KILL_MONSTER, getMonsterId());
            p.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_CLEAR_GROUP_MONSTER, getGroupId());
        });

        SceneGroupInstance groupInstance = scene.getScriptManager().getGroupInstanceById(getGroupId());
        if(groupInstance != null && getMetaMonster() != null)
            groupInstance.getDeadEntities().add(getMetaMonster().config_id);

        scene.triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_KILL_GROUP_MONSTER, getGroupId());
        scene.triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_KILL_TYPE_MONSTER, getMonsterData().getType().getValue());
        scene.triggerDungeonEvent(DungeonPassConditionType.DUNGEON_COND_KILL_MONSTER, getMonsterId());
    }

    public void recalcStats() {
        // Monster data
        MonsterData data = getMonsterData();

        float maxHp = getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
        // Get hp percent, set to 100% if none
        float hpPercent = maxHp <= 0 ? 1f : getFightProperty(FightProperty.FIGHT_PROP_CUR_HP) / maxHp;

        // Clear properties
        getFightProperties().clear();

        // Base stats
        MonsterData.definedFightProperties.forEach(prop -> setFightProperty(prop, data.getFightProperty(prop)));

        // Level curve
        MonsterCurveData curve = GameData.getMonsterCurveDataMap().get(getLevel());
        if (curve != null) {
            data.getPropGrowCurves().forEach(growCurve -> {
                FightProperty prop = FightProperty.getPropByName(growCurve.getType());
                setFightProperty(prop, getFightProperty(prop) * curve.getMultByProp(growCurve.getGrowCurve()));
            });
        }

        // Set % stats
        FightProperty.forEachCompoundProperty(c -> setFightProperty(c.getResult(),
            getFightProperty(c.getFlat()) + (getFightProperty(c.getBase()) * (1f + getFightProperty(c.getPercent())))));

        // Set current hp
        setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, getFightProperty(FightProperty.FIGHT_PROP_MAX_HP) * hpPercent);
    }

    @Override
    public SceneEntityInfo toProto() {
        EntityAuthorityInfo authority = EntityAuthorityInfo.newBuilder()
            .setAbilityInfo(AbilitySyncStateInfo.newBuilder())
            .setRendererChangedInfo(EntityRendererChangedInfo.newBuilder())
            .setAiInfo(SceneEntityAiInfo.newBuilder().setIsAiOpen(true).setBornPos(getBornPos().toProto()))
            .setBornPos(getBornPos().toProto())
            .build();

        SceneEntityInfo.Builder entityInfo = SceneEntityInfo.newBuilder()
            .setEntityId(getId())
            .setEntityType(ProtEntityType.PROT_ENTITY_TYPE_MONSTER)
            .setMotionInfo(getMotionInfo())
            .addAnimatorParaList(AnimatorParameterValueInfoPair.newBuilder())
            .setEntityClientData(EntityClientData.newBuilder())
            .setEntityAuthorityInfo(authority)
            .setLifeState(getLifeState().getValue());

        addAllFightPropsToEntityInfo(entityInfo);

        entityInfo.addPropList(PropPair.newBuilder()
            .setType(PlayerProperty.PROP_LEVEL.getId())
            .setPropValue(ProtoHelper.newPropValue(PlayerProperty.PROP_LEVEL, getLevel()))
            .build());

        SceneMonsterInfo.Builder monsterInfo = SceneMonsterInfo.newBuilder()
            .setMonsterId(getMonsterId())
            .setGroupId(getGroupId())
            .setConfigId(getConfigId())
            .addAllAffixList(getMonsterData().getAffix())
            .setAuthorityPeerId(getWorld().getHostPeerId())
            .setPoseId(getPoseId())
            .setBlockId(getScene().getId())
            .setBornType(MonsterBornType.MONSTER_BORN_TYPE_DEFAULT);

        if(getMetaMonster() != null && getMetaMonster().special_name_id != 0){
            monsterInfo.setTitleId(getMetaMonster().title_id)
                .setSpecialNameId(getMetaMonster().special_name_id);
        } else if (getMonsterData().getDescribeData() != null) {
            monsterInfo.setTitleId(getMonsterData().getDescribeData().getTitleId())
                .setSpecialNameId(getMonsterData().getSpecialNameId());
        }

        if (getMonsterWeaponId() > 0) {
            SceneWeaponInfo weaponInfo = SceneWeaponInfo.newBuilder()
                .setEntityId(getWeaponEntityId())
                .setGadgetId(getMonsterWeaponId())
                .setAbilityInfo(AbilitySyncStateInfo.newBuilder())
                .build();

            monsterInfo.addWeaponList(weaponInfo);
        }
        if (getAiId() != -1) {
            monsterInfo.setAiConfigId(getAiId());
        }

        entityInfo.setMonster(monsterInfo);

        return entityInfo.build();
    }
}
