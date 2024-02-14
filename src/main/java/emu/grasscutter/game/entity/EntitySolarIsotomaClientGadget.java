package emu.grasscutter.game.entity;

import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.platform.EntitySolarIsotomaElevatorPlatform;
import emu.grasscutter.game.world.Scene;
import lombok.Getter;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.battle.event.EvtCreateGadgetNotify;

public class EntitySolarIsotomaClientGadget extends EntityClientGadget {
    public static final int GADGET_ID = 41038001;
    public static final int ELEVATOR_GADGET_ID = 41038002;
    @Getter private EntityGadget platformGadget;

    public EntitySolarIsotomaClientGadget(Scene scene, EvtCreateGadgetNotify notify, CreateGadgetEntityConfig createConfig) {
        super(scene, notify, createConfig);
    }

    @Override
    public void onCreate() {
        //Create solar isotoma elevator and send to all.
        val config = new CreateGadgetEntityConfig(ELEVATOR_GADGET_ID)
            .setOwner(this);
        this.platformGadget = new EntitySolarIsotomaElevatorPlatform(this, getScene(), config);
        getScene().addEntity(this.platformGadget);
    }

    @Override
    public void onRemoved() {
        //Remove solar isotoma elevator entity.
        getScene().removeEntity(this.platformGadget);
    }
}
