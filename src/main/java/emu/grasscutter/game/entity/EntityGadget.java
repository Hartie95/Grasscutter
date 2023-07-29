package emu.grasscutter.game.entity;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.config.ConfigEntityGadget;
import emu.grasscutter.data.binout.config.fields.ConfigAbilityData;
import emu.grasscutter.data.excels.GadgetData;
import emu.grasscutter.data.server.GadgetMapping;
import emu.grasscutter.game.entity.gadget.*;
import emu.grasscutter.game.entity.gadget.platform.BaseRoute;
import emu.grasscutter.game.managers.blossom.BlossomManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.props.PlayerProperty;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.proto.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import emu.grasscutter.net.proto.AnimatorParameterValueInfoPairOuterClass.AnimatorParameterValueInfoPair;
import emu.grasscutter.net.proto.EntityAuthorityInfoOuterClass.EntityAuthorityInfo;
import emu.grasscutter.net.proto.EntityClientDataOuterClass.EntityClientData;
import emu.grasscutter.net.proto.EntityRendererChangedInfoOuterClass.EntityRendererChangedInfo;
import emu.grasscutter.net.proto.GadgetInteractReqOuterClass.GadgetInteractReq;
import emu.grasscutter.net.proto.MotionInfoOuterClass.MotionInfo;
import emu.grasscutter.net.proto.PlatformInfoOuterClass;
import emu.grasscutter.net.proto.PropPairOuterClass.PropPair;
import emu.grasscutter.net.proto.ProtEntityTypeOuterClass.ProtEntityType;
import emu.grasscutter.net.proto.SceneEntityAiInfoOuterClass.SceneEntityAiInfo;
import emu.grasscutter.net.proto.SceneEntityInfoOuterClass.SceneEntityInfo;
import emu.grasscutter.net.proto.SceneGadgetInfoOuterClass.SceneGadgetInfo;
import emu.grasscutter.net.proto.VectorOuterClass.Vector;
import emu.grasscutter.net.proto.VisionTypeOuterClass;
import emu.grasscutter.scripts.EntityControllerScriptManager;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneGadget;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.packet.send.PacketGadgetStateNotify;
import emu.grasscutter.server.packet.send.PacketPlatformStartRouteNotify;
import emu.grasscutter.server.packet.send.PacketPlatformStopRouteNotify;
import emu.grasscutter.server.packet.send.PacketSceneTimeNotify;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.ProtoHelper;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import javax.annotation.Nullable;
import java.util.*;

@ToString(callSuper = true)
public class EntityGadget extends EntityBaseGadget {
    @Getter private final GadgetData gadgetData;
    @Getter(onMethod = @__(@Override)) @Setter
    private int gadgetId;
    @Getter private final Position bornPos;
    @Getter private final Position bornRot;
    @Getter @Setter private GameEntity owner = null;
    @Getter @Setter private List<GameEntity> children = new ArrayList<>();
    @Getter private int state;
    @Getter @Setter private int pointType;
    @Getter private GadgetContent content;
    @Getter(onMethod = @__(@Override), lazy = true)
    private final Int2FloatMap fightProperties = new Int2FloatOpenHashMap();
    @Getter @Setter private SceneGadget metaGadget;
    @Nullable @Getter
    private ConfigEntityGadget configGadget;
    @Getter @Setter private BaseRoute routeConfig;
    @Getter @Setter private int stopValue = 0; // Controller related, inited to zero
    @Getter @Setter private int startValue = 0; // Controller related, inited to zero
    @Getter @Setter private int ticksSinceChange;

    public EntityGadget(Scene scene, int gadgetId, Position pos) {
        this(scene, gadgetId, pos, null, null);
    }

    public EntityGadget(Scene scene, int gadgetId, Position pos, Position rot) {
        this(scene, gadgetId, pos, rot, null);
    }

    public EntityGadget(Scene scene, int gadgetId, Position pos, Position rot, GadgetContent content) {
        super(scene, pos, rot);
        this.gadgetData = GameData.getGadgetDataMap().get(gadgetId);
        this.id = getScene().getWorld().getNextEntityId(EntityIdType.GADGET);
        this.gadgetId = gadgetId;
        this.content = content;
        this.bornPos = getPosition().clone();
        this.bornRot = getRotation().clone();

        Optional.ofNullable(getGadgetData())
            .map(GadgetData::getJsonName)
            .ifPresent(jsonName -> this.configGadget = GameData.getGadgetConfigData().get(jsonName));

        fillFightProps(this.configGadget);

        Optional.ofNullable(GameData.getGadgetMappingMap().get(gadgetId))
            .map(GadgetMapping::getServerController)
            .map(EntityControllerScriptManager::getGadgetController)
            .ifPresent(this::setEntityController);

        addConfigAbilities();
    }

    private void addConfigAbilities(){
        Optional.ofNullable(getConfigGadget())
            .map(ConfigEntityGadget::getAbilities).stream()
            .flatMap(Collection::stream)
            .forEach(this::addConfigAbility);
    }

    private void addConfigAbility(ConfigAbilityData abilityData){
        Optional.ofNullable(GameData.getAbilityData(abilityData.getAbilityName()))
            .ifPresent(data -> getScene().getWorld().getHost().getAbilityManager()
                .addAbilityToEntity(this, data, abilityData.getAbilityID()));
    }

    public void setState(int state) {
        this.state = state;
        Optional.ofNullable(getMetaGadget()) // Cache the gadget state
            .map(g -> g.group).map(g -> g.id)
            .map(getScene().getScriptManager()::getCachedGroupInstanceById)
            .ifPresent(instance -> instance.cacheGadgetState(getMetaGadget(), state));
    }

    public void updateState(int state) {
        if(state == getState()) return; //Don't triggers events

        setState(state);
        setTicksSinceChange(getScene().getSceneTimeSeconds());
        getScene().broadcastPacket(new PacketGadgetStateNotify(this, state));
        getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_GADGET_STATE_CHANGE, state, getConfigId()));
    }

    @Deprecated(forRemoval = true) // Dont use!
    public void setContent(GadgetContent content) {
        this.content = Optional.ofNullable(this.content).orElse(content);
    }

    // TODO refactor
    public void buildContent() {
        if (getContent() != null || getGadgetData() == null || getGadgetData().getType() == null) return;

        this.content = switch (getGadgetData().getType()) {
            case GatherPoint -> new GadgetGatherPoint(this);
            case GatherObject -> new GadgetGatherObject(this);
            case Worktop, SealGadget -> new GadgetWorktop(this);
            case RewardStatue -> new GadgetRewardStatue(this);
            case Chest -> new GadgetChest(this);
            case Gadget -> new GadgetObject(this);
            default -> null;
        };
    }

    @Override
    public void onInteract(Player player, GadgetInteractReq interactReq) {
        Optional.ofNullable(getContent())
            .filter(c -> c.onInteract(player, interactReq))
            .ifPresent(s -> getScene().killEntity(this));
    }

    @Override
    public void onCreate() {
        // Lua event
        getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_GADGET_CREATE, getConfigId()));
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if(this.children.isEmpty()) return;

        getScene().removeEntities(this.children, VisionTypeOuterClass.VisionType.VISION_TYPE_REMOVE);
        this.children.clear();
    }

    @Override
    public void onDeath(int killerId) {
        super.onDeath(killerId); // Invoke super class's onDeath() method.

        Optional.ofNullable(getSpawnEntry()).ifPresent(getScene().getDeadSpawnedEntities()::add);
        getScene().getScriptManager().callEvent(new ScriptArgs(getGroupId(), EventType.EVENT_ANY_GADGET_DIE, getConfigId()));

        Optional.ofNullable(getScene().getScriptManager().getCachedGroupInstanceById(getGroupId()))
            .ifPresent(groupInstance -> Optional.ofNullable(getMetaGadget())
                .map(g -> g.config_id)
                .ifPresent(groupInstance.getDeadEntities()::add));

        BlossomManager hostBlossom = getScene().getWorld().getHost().getBlossomManager();
        val removedChest = Optional.ofNullable(hostBlossom)
            .map(BlossomManager::getSpawnedChest)
            .map(chestMap -> chestMap.remove(getConfigId()));
        if (removedChest.isPresent()) {
            Grasscutter.getLogger().info("[EntityGadget] Removing Blossom Chest: {}, {}", getGroupId(), getConfigId());
            getScene().unregisterDynamicGroup(getGroupId());
            getScene().getScriptManager().callEvent(
                new ScriptArgs(getGroupId(), EventType.EVENT_BLOSSOM_CHEST_DIE, getConfigId()));
            hostBlossom.buildNextCamp(getGroupId());
        }
    }

    public boolean startPlatform(){
        if(getRouteConfig() == null) return false;

        if(getRouteConfig().isStarted()) return true;

        getScene().broadcastPacket(new PacketSceneTimeNotify(getScene()));
        getRouteConfig().startRoute(getScene());
        getScene().broadcastPacket(new PacketPlatformStartRouteNotify(this));
        return true;
    }

    public boolean stopPlatform(){
        if(getRouteConfig() == null) return false;

        if(!getRouteConfig().isStarted()) return true;

        getRouteConfig().stopRoute(getScene());
        getScene().broadcastPacket(new PacketPlatformStopRouteNotify(this));
        return true;
    }

    @Override
    public SceneEntityInfo toProto() {
        EntityAuthorityInfo authority = EntityAuthorityInfo.newBuilder()
                .setAbilityInfo(AbilitySyncStateInfo.newBuilder())
                .setRendererChangedInfo(EntityRendererChangedInfo.newBuilder())
                .setAiInfo(SceneEntityAiInfo.newBuilder().setIsAiOpen(true).setBornPos(bornPos.toProto()))
                .setBornPos(bornPos.toProto())
                .build();

        SceneEntityInfo.Builder entityInfo = SceneEntityInfo.newBuilder()
                .setEntityId(getId())
                .setEntityType(ProtEntityType.PROT_ENTITY_TYPE_GADGET)
                .setMotionInfo(MotionInfo.newBuilder().setPos(getPosition().toProto()).setRot(getRotation().toProto()).setSpeed(Vector.newBuilder()))
                .addAnimatorParaList(AnimatorParameterValueInfoPair.newBuilder())
                .setEntityClientData(EntityClientData.newBuilder())
                .setEntityAuthorityInfo(authority)
                .setLifeState(1);

        entityInfo.addPropList(PropPair.newBuilder()
            .setType(PlayerProperty.PROP_LEVEL.getId())
            .setPropValue(ProtoHelper.newPropValue(PlayerProperty.PROP_LEVEL, 1))
            .build());

        // We do not use the getter to null check because the getter will create a fight prop map if it is null
        Optional.ofNullable(this.fightProperties).ifPresent(s -> addAllFightPropsToEntityInfo(entityInfo));

        SceneGadgetInfo.Builder gadgetInfo = SceneGadgetInfo.newBuilder()
                .setGadgetId(getGadgetId())
                .setGroupId(getGroupId())
                .setConfigId(getConfigId())
                .setGadgetState(getState())
                .setIsEnableInteract(getGadgetData().isInteractive())
                .setAuthorityPeerId(getScene().getWorld().getHostPeerId());

        Optional.ofNullable(getMetaGadget()).map(g -> g.draft_id).ifPresent(gadgetInfo::setDraftId);
        Optional.ofNullable(getOwner()).map(GameEntity::getId).ifPresent(gadgetInfo::setOwnerEntityId);
        Optional.ofNullable(getContent()).ifPresent(c -> c.onBuildProto(gadgetInfo));
        Optional.ofNullable(getRouteConfig()).ifPresent(s -> gadgetInfo.setPlatform(getPlatformInfo()));
        return entityInfo.setGadget(gadgetInfo).build();
    }

    public PlatformInfoOuterClass.PlatformInfo.Builder getPlatformInfo(){
        return Optional.ofNullable(getRouteConfig())
                .map(BaseRoute::toProto)
                .orElse(PlatformInfoOuterClass.PlatformInfo.newBuilder());
    }
}
