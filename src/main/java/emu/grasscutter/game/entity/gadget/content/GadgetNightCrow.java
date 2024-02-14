package emu.grasscutter.game.entity.gadget.content;

import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.player.Player;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.NightCrowGadgetInfo;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneGadgetInfo;

import java.util.List;

public class GadgetNightCrow extends GadgetContent {
    public GadgetNightCrow(EntityGadget gadget) {
        super(gadget);
    }

    @Override
    public boolean onInteract(Player player, GadgetInteractReq req) {
        return false;
    }

    @Override
    public void onBuildProto(SceneGadgetInfo gadgetInfo) {
        val arguments = getGadget().getArguments();
        val content = new NightCrowGadgetInfo(arguments != null ? arguments: List.of());
        gadgetInfo.setContent(new SceneGadgetInfo.Content.NightCrowGadgetInfo(content));
    }
}
