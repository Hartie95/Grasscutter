package emu.grasscutter.game.entity.gadget.content;

import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.player.Player;
import lombok.val;
import org.anime_game_servers.gi_lua.models.scene.group.SceneGadget;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.OfferingInfo;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneGadgetInfo;

public class GadgetOffering extends GadgetContent {
    public GadgetOffering(EntityGadget gadget) {
        super(gadget);
    }

    @Override
    public boolean onInteract(Player player, GadgetInteractReq req) {
        return false;
    }

    @Override
    public void onBuildProto(SceneGadgetInfo gadgetInfo) {
        val configSource = getGadget().getSpawnConfig().getInitDataSource();
        val content = new OfferingInfo();
        // TODO make independend from SceneGadget?
        if(configSource instanceof SceneGadget sceneGadget && sceneGadget.getOfferingConfig()!=null){
            content.setOfferingId(sceneGadget.getOfferingConfig().getOfferingId());
        }
        gadgetInfo.setContent(new SceneGadgetInfo.Content.OfferingInfo(content));
    }
}
