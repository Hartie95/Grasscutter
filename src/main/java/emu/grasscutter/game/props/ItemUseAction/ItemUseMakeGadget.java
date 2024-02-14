package emu.grasscutter.game.props.ItemUseAction;

import emu.grasscutter.game.entity.EntityVehicle;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.props.ItemUseOp;

public class ItemUseMakeGadget extends ItemUseInt {
    @Override
    public ItemUseOp getItemUseOp() {
        return ItemUseOp.ITEM_USE_MAKE_GADGET;
    }

    public ItemUseMakeGadget(String[] useParam) {
        super(useParam);
    }

    @Override
    public boolean useItem(UseItemParams params) {
        var player = params.player;
        var scene = player.getScene();
        var pos = player.getPosition().nearby2d(1f);
        var rot = player.getRotation().clone();
        CreateGadgetEntityConfig config = new CreateGadgetEntityConfig(this.i)
            .setBornPos(pos)
            .setBornRot(rot);

        // TODO this should probably not be a vehicle, since its used for spawning gadget 70300080 and 70800017
        var e = new EntityVehicle(scene, player, config);
        scene.addEntity(e);
        return true;
    }
}
