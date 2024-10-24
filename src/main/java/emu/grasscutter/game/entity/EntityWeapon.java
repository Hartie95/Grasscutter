package emu.grasscutter.game.entity;

import javax.annotation.Nullable;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.config.fields.ConfigAbilityData;
import emu.grasscutter.game.ability.AbilityManager;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.gadget.content.GadgetContent;
import emu.grasscutter.game.entity.interfaces.ConfigAbilityDataAbilityEntity;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.EntityControllerScriptManager;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import lombok.Getter;
import lombok.ToString;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneEntityInfo;

import java.util.Collection;

@ToString(callSuper = true)
public class EntityWeapon extends EntityBaseGadget implements ConfigAbilityDataAbilityEntity {

    private Int2FloatMap fightProperties;

    public EntityWeapon(Scene scene, CreateGadgetEntityConfig createConfig) {
        super(scene, createConfig);
        if(GameData.getGadgetMappingMap().containsKey(gadgetId)) {
            String controllerName = GameData.getGadgetMappingMap().get(gadgetId).getServerController();
            setEntityController(EntityControllerScriptManager.getGadgetController(controllerName));
            if(getEntityController() == null) {
                Grasscutter.getLogger().warn("Gadget controller {} not found", controllerName);
            }
        }

        this.id = scene.getWorld().getNextEntityId(EntityIdType.WEAPON);
        Grasscutter.getLogger().warn("New weapon entity id {} at scene {}", this.id, this.getScene().getId());
    }
    @Override
    public Int2FloatMap getFightProperties() {
        if(fightProperties == null){
            fightProperties = new Int2FloatOpenHashMap();
        }
        return fightProperties;
    }

    @Override
    public SceneEntityInfo toProto() {
        return null;
    }

    @Override
    public GadgetContent buildContent(CreateGadgetEntityConfig config) {
        return null;
    }
}
