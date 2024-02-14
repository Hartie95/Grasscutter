package emu.grasscutter.game.entity;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.gadget.content.GadgetViewPoint;
import emu.grasscutter.game.entity.gadget.content.*;
import emu.grasscutter.game.entity.gadget.platform.BaseRoute;
import emu.grasscutter.game.entity.gadget.platform.ConfigRoute;
import emu.grasscutter.game.entity.gadget.platform.PointArrayRoute;
import emu.grasscutter.game.entity.interfaces.ConfigAbilityDataAbilityEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.props.PlayerProperty;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.game.world.SceneGroupInstance;
import emu.grasscutter.game.world.SpawnDataEntry;
import emu.grasscutter.scripts.EntityControllerScriptManager;
import emu.grasscutter.server.packet.send.PacketGadgetStateNotify;
import emu.grasscutter.server.packet.send.PacketPlatformStartRouteNotify;
import emu.grasscutter.server.packet.send.PacketPlatformStopRouteNotify;
import emu.grasscutter.server.packet.send.PacketSceneTimeNotify;
import emu.grasscutter.utils.ProtoHelper;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import lombok.*;
import org.anime_game_servers.gi_lua.models.ScriptArgs;
import org.anime_game_servers.gi_lua.models.constants.EventType;
import org.anime_game_servers.gi_lua.models.scene.group.SceneGadget;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.general.ability.AbilitySyncStateInfo;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.*;

import java.util.*;
import java.util.List;

@ToString(callSuper = true)
public class EntityGadget extends EntityBaseGadget implements ConfigAbilityDataAbilityEntity {

    @Getter @Setter private int pointType;
    private Int2FloatMap fightProperties;
    @Getter @Setter private SceneGadget metaGadget;
    @Getter @Setter private BaseRoute routeConfig;
    @Getter @Setter private int draftId;
    @Getter @Setter private int chestDropId;
    @Getter @Setter private boolean chestShowCutscene = false;

    @Getter boolean isPersistent = false;

    @Getter @Setter private int stopValue = 0; //Controller related, inited to zero
    @Getter @Setter private int startValue = 0; //Controller related, inited to zero


    public EntityGadget(Scene scene, CreateGadgetEntityConfig createData) {
        super(scene, createData);
        this.id = this.getScene().getWorld().getNextEntityId(EntityIdType.GADGET);
        this.pointType = createData.getPointType();
        this.routeConfig = createData.getRouteConfig();
        if(createData.getInitDataSource() instanceof SceneGadget gadget)
            this.metaGadget = gadget;

        if(GameData.getGadgetMappingMap().containsKey(gadgetId)) {
            String controllerName = GameData.getGadgetMappingMap().get(gadgetId).getServerController();
            setEntityController(EntityControllerScriptManager.getGadgetController(controllerName));
            if(getEntityController() == null) {
                Grasscutter.getLogger().warn("Gadget controller {} not found", controllerName);
            }
        }
        this.draftId = createData.getDraftId();
        this.chestDropId = createData.getChestDropId();
        this.chestShowCutscene = createData.isChestShowCutscene();
    }

    @Override
    public Int2FloatMap getFightProperties() {
        if(fightProperties == null){
            fightProperties = new Int2FloatOpenHashMap();
        }
        return fightProperties;
    }

    @Override
    public void setInteractEnabled(boolean enable) {
        super.setInteractEnabled(enable);
        this.getScene().broadcastPacket(new PacketGadgetStateNotify(this, this.getState())); //Update the interact
    }

    @Override
    public void setState(int state) {
        super.setState(state);
        val groupId = getGroupId();
        //Cache the gadget state
        if(groupId > 0) {
            var instance = getScene().getScriptManager().getCachedGroupInstanceById(groupId);
            if(instance != null) instance.cacheGadgetState(this);
        }
    }

    // TODO refactor
    @Override
    public GadgetContent buildContent(CreateGadgetEntityConfig config) {
        if (this.getGadgetData() == null || this.getGadgetData().getType() == null) {
            return null;
        }

        return switch (this.getGadgetData().getType()) {
            case GatherPoint -> new GadgetGatherPoint(this);
            case GatherObject -> new GadgetGatherObject(this);
            case Worktop, SealGadget -> new GadgetWorktop(this);
            case RewardStatue -> new GadgetRewardStatue(this);
            case Chest -> new GadgetChest(this);
            case Gadget, Platform -> new GadgetObject(this);
            case Screen -> new GadgetScreen(this);
            case ViewPoint -> new GadgetViewPoint(this);
            case NightCrowGadget -> new GadgetNightCrow(this);
            case DeshretObeliskGadget -> new GadgetDeshretObelisk(this);
            case OfferingGadget -> new GadgetOffering(this);
            default -> null;
        };
    }

    @Override
    public void onInteract(Player player, GadgetInteractReq interactReq) {
        if(!isInteractEnabled()) return;

        if (this.getContent() == null) {
            val contentName = GameData.getGadgetDataMap().get(interactReq.getGadgetId()).getType().name();
            Grasscutter.getLogger().warn("Missing Gadget content: {}", contentName);
            return;
        }

        boolean shouldDelete = this.getContent().onInteract(player, interactReq);

        if (shouldDelete) {
            this.getScene().killEntity(this);
        }
    }

    @Override
    public void onCreate() {
        // Lua event
        getScene().getScriptManager().callEvent(new ScriptArgs(this.getGroupId(), EventType.EVENT_GADGET_CREATE, this.getConfigId())
            .setSourceEntityId(this.getId()));
    }

    @Override
    public void afterCreate(List<Player> players) {
        if (this.getMetaGadget() != null && !this.getMetaGadget().isStartRoute()) return;
        if (this.routeConfig == null) return;
        this.routeConfig.startRoute(this.getScene());
        players.forEach(p -> p.sendPacket(new PacketPlatformStartRouteNotify(this)));
    }


    @Override
    public void onDeath(int killerId) {
        super.onDeath(killerId); // Invoke super class's onDeath() method.

        if (this.getSpawnConfig() != null && this.getSpawnConfig().getInitDataSource() instanceof SpawnDataEntry spawnEntry) {
            this.getScene().getDeadSpawnedEntities().add(spawnEntry);
        }
        getScene().getScriptManager().callEvent(new ScriptArgs(this.getGroupId(), EventType.EVENT_ANY_GADGET_DIE, this.getConfigId()));

        SceneGroupInstance groupInstance = getScene().getScriptManager().getCachedGroupInstanceById(this.getGroupId());
        if(groupInstance != null && getConfigId() > 0)
            groupInstance.getDeadEntities().add(getConfigId());

        val hostBlossom = getScene().getWorld().getHost().getBlossomManager();
        val removedChest = hostBlossom.getSpawnedChest().remove(getConfigId());
        if (removedChest != null) {
            Grasscutter.getLogger().info("[EntityGadget] Removing Blossom Chest: {}, {}", getGroupId(), getConfigId());
            getScene().unregisterDynamicGroup(getGroupId());
            getScene().getScriptManager().callEvent(
                new ScriptArgs(getGroupId(), EventType.EVENT_BLOSSOM_CHEST_DIE, getConfigId()));
            hostBlossom.buildNextCamp(getGroupId());
        }
    }

    public boolean startPlatform(){
        if(routeConfig == null){
            return false;
        }

        if (routeConfig.isStarted()) {
            return true;
        }

        schedulePlatform();

        getScene().broadcastPacket(new PacketSceneTimeNotify(getScene()));
        routeConfig.startRoute(getScene());
        getScene().broadcastPacket(new PacketPlatformStartRouteNotify(this));

        return true;
    }

    public boolean scheduleArrayPoints(int pointArrayId, List<Integer> platformPointList) {
        if (platformPointList.size() > 1)
            Grasscutter.getLogger().warn("Untested PointArrayRoute with multiple points! Keep an eye on Route {} at pos: {}", pointArrayId, getBornPos());

        if (!(routeConfig instanceof PointArrayRoute pointArrayRoute)) {
            Grasscutter.getLogger().error("routeConfig not instance of PointArrayRoute");
            return false;
        }

        var route = this.getScene().getPointArrayById(pointArrayId);

        if (route == null) {
            Grasscutter.getLogger().error("Cannot find route with ID {}", pointArrayId);
            return false;
        }

        var points = route.getPlatformPointList();

        val routePointList = platformPointList.stream().map(x -> Arrays.stream(points).filter(y -> y.getPointId() == x).findFirst().orElse(null).toProto()).toList();
        pointArrayRoute.setRoutePoints(routePointList);

        pointArrayRoute.startRoute(getScene());
        pointArrayRoute.setStartSceneTime(getScene().getSceneTime() + 2000); //todo: read routePointList.get(0).getMoveParams() for this offest

        return true;
    }

    public boolean schedulePlatform() {
        if (!(routeConfig instanceof ConfigRoute configRoute)) {
            return false;
        }

        var route = this.getScene().getSceneRouteById(configRoute.getRouteId());

        if (route == null) {
            return false;
        }

        var points = route.getPoints();

        //restart platform if at end and told to start
        if (configRoute.getStartIndex() == points.length - 1) {
            configRoute.setStartIndex(0);
        }

        val currIndex = configRoute.getStartIndex();
        if (currIndex == 0) {
            this.getScene().callPlatformEvent(this.getId());
        }

        double distance = points[currIndex].getPos().computeDistance(points[currIndex + 1].getPos());
        double time = 1000 * distance / points[currIndex].getTargetVelocity();
        time += this.getScene().getSceneTime();
        this.getScene().getScheduledPlatforms().put(this.getId(), time);

        return true;
    }

    public boolean stopPlatform(){
        if(routeConfig == null){
            return false;
        }
        this.getScene().getScheduledPlatforms().remove(this.getId());
        routeConfig.stopRoute(getScene());
        getScene().broadcastPacket(new PacketPlatformStopRouteNotify(this));

        return true;
    }

    @Override
    public SceneEntityInfo toProto() {
        val protoBornPos = getBornPos().toProto();
        val protoCurPos = getPosition().toProto();
        val protoCurRot = getRotation().toProto();
        val authority = new EntityAuthorityInfo(new AbilitySyncStateInfo(), new EntityRendererChangedInfo(),
            new SceneEntityAiInfo(true, protoBornPos), protoBornPos);

        val entityInfo = new SceneEntityInfo(ProtEntityType.PROT_ENTITY_GADGET, getId());
        entityInfo.setMotionInfo(new MotionInfo(protoCurPos, protoCurRot, new org.anime_game_servers.multi_proto.gi.messages.general.Vector()));
        entityInfo.setAnimatorParaList(List.of(new AnimatorParameterValueInfoPair()));
        entityInfo.setEntityClientData(new EntityClientData());
        entityInfo.setEntityAuthorityInfo(authority);
        entityInfo.setLifeState(1);

        val pair = new PropPair(PlayerProperty.PROP_LEVEL.getId(), ProtoHelper.newPropValue(PlayerProperty.PROP_LEVEL, getLevel()));
        entityInfo.setPropList(List.of(pair));

        // We do not use the getter to null check because the getter will create a fight prop map if it is null
        if (this.fightProperties != null) {
            addAllFightPropsToEntityInfo(entityInfo);
        }

        val gadgetInfo = new SceneGadgetInfo(this.getGadgetId(), this.getGroupId(), this.getConfigId());
        gadgetInfo.setGadgetState(this.getState());
        gadgetInfo.setEnableInteract(this.isInteractEnabled());
        gadgetInfo.setInteractId(this.getInteractId());
        gadgetInfo.setAuthorityPeerId(this.getScene().getWorld().getHostPeerId());
        gadgetInfo.setShowCutscene(this.chestShowCutscene);

        if (this.draftId > 0) {
            gadgetInfo.setDraftId(this.draftId);
        }

        if(getOwnerEntity() != null){
            gadgetInfo.setOwnerEntityId(getOwnerEntity().getId());
        }

        if (this.getContent() != null) {
            this.getContent().onBuildProto(gadgetInfo);
        }

        if(routeConfig!=null){
            gadgetInfo.setPlatform(getPlatformInfo());
        }

        entityInfo.setEntity(new SceneEntityInfo.Entity.Gadget(gadgetInfo));

        return entityInfo;
    }

    public PlatformInfo getPlatformInfo(){
        if(routeConfig != null){
            return routeConfig.toProto();
        }

        return new PlatformInfo();
    }
}
