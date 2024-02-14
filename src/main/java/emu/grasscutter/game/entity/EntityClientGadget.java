package emu.grasscutter.game.entity;

import emu.grasscutter.game.ability.AbilityManager;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.gadget.content.GadgetClient;
import emu.grasscutter.game.entity.gadget.content.GadgetContent;
import emu.grasscutter.game.entity.interfaces.ConfigAbilityDataAbilityEntity;
import emu.grasscutter.game.props.PlayerProperty;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.utils.ProtoHelper;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import lombok.Getter;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.battle.event.EvtCreateGadgetNotify;
import org.anime_game_servers.multi_proto.gi.messages.general.Vector;
import org.anime_game_servers.multi_proto.gi.messages.general.ability.AbilitySyncStateInfo;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.*;

import java.util.List;

@Getter
public class EntityClientGadget extends EntityBaseGadget implements ConfigAbilityDataAbilityEntity {
    public EntityClientGadget(Scene scene, EvtCreateGadgetNotify notify, CreateGadgetEntityConfig createConfig) {
        super(scene, createConfig);
        this.id = notify.getEntityId();
    }

    @Override
    public GadgetContent buildContent(CreateGadgetEntityConfig config) {
        return new GadgetClient(this);
    }

    public int getOriginalOwnerEntityId() {
        if(getContent() instanceof GadgetClient gadgetClient) {
            return gadgetClient.getOriginalOwnerEntityId();
        }
        return 0;
    }

    @Override
    public AbilityManager getAbilityTargetManager() {
        return getSpawnConfig().getPlayerOwner().getAbilityManager();
    }

    @Override
    public void onDeath(int killerId) {
        super.onDeath(killerId); // Invoke super class's onDeath() method.
    }

    @Override
    public Int2FloatMap getFightProperties() {
        return null;
    }

    @Override
    public SceneEntityInfo toProto() {
        val protoBornPos = new Vector();
        val aiInfo = new SceneEntityAiInfo(true, protoBornPos);
        val authority = new EntityAuthorityInfo(new AbilitySyncStateInfo(), new EntityRendererChangedInfo(), aiInfo, protoBornPos);

        val entityInfo = new SceneEntityInfo(ProtEntityType.PROT_ENTITY_GADGET, getId());
        entityInfo.setMotionInfo(new MotionInfo(getPosition().toProto(), getRotation().toProto(), new Vector()));
        entityInfo.setAnimatorParaList(List.of(new AnimatorParameterValueInfoPair()));
        entityInfo.setEntityClientData(new EntityClientData());
        entityInfo.setEntityAuthorityInfo(authority);
        entityInfo.setLifeState(1);

        val pair = new PropPair(PlayerProperty.PROP_LEVEL.getId(), ProtoHelper.newPropValue(PlayerProperty.PROP_LEVEL, getLevel()));
        entityInfo.setPropList(List.of(pair));


        val gadgetInfo = new SceneGadgetInfo(this.getGadgetId());
        gadgetInfo.setEnableInteract(true);
        gadgetInfo.setAuthorityPeerId(this.getOwner().getPeerId());
        if (getContent()!=null) {
            getContent().onBuildProto(gadgetInfo);
        }

        entityInfo.setEntity(new SceneEntityInfo.Entity.Gadget(gadgetInfo));

        return entityInfo;
    }
}
