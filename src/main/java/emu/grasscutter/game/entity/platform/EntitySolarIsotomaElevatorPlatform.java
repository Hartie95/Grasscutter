package emu.grasscutter.game.entity.platform;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.binout.config.ConfigEntityGadget;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.gadget.content.GadgetAbility;
import emu.grasscutter.game.entity.gadget.content.GadgetContent;
import emu.grasscutter.game.entity.gadget.platform.AbilityRoute;
import emu.grasscutter.game.world.Scene;

public class EntitySolarIsotomaElevatorPlatform extends EntityGadget {
    private final EntitySolarIsotomaClientGadget isotoma;
    public EntitySolarIsotomaElevatorPlatform(EntitySolarIsotomaClientGadget isotoma, Scene scene, CreateGadgetEntityConfig createConfig) {
        super(scene, createConfig);
        this.setRouteConfig(new AbilityRoute(createConfig, false, false));
        this.isotoma = isotoma;
    }

    @Override
    public GadgetContent buildContent(CreateGadgetEntityConfig config) {
        return new GadgetAbility(this, isotoma);
    }

    @Override
    protected void fillFightProps(ConfigEntityGadget configGadget) {
        if (configGadget == null || configGadget.getCombat() == null) {
            return;
        }
        var combatData = configGadget.getCombat();
        var combatProperties = combatData.getProperty();

        if (combatProperties.isUseCreatorProperty()) {
            //If useCreatorProperty == true, use owner's property;
            GameEntity ownerEntity = getOwnerEntity();
            if (ownerEntity != null && ownerEntity.getFightProperties() != null && getFightProperties() != null) {
                getFightProperties().putAll(ownerEntity.getFightProperties());
                return;
            } else {
                if(ownerEntity == null) {
                    Grasscutter.getLogger().warn("Why gadget owner is null?");
                }
            }
        }

        super.fillFightProps(configGadget);
    }
}
