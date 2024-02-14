package emu.grasscutter.game.entity.gadget.content;

import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.player.Player;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.DeshretObeliskGadgetInfo;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneGadgetInfo;

import java.util.List;

public class GadgetDeshretObelisk extends GadgetContent {
    public GadgetDeshretObelisk(EntityGadget gadget) {
        super(gadget);
    }

    @Override
    public boolean onInteract(Player player, GadgetInteractReq req) {
        return false;
    }

    @Override
    public void onBuildProto(SceneGadgetInfo gadgetInfo) {
        val arguments = getGadget().getArguments();
        val content = new DeshretObeliskGadgetInfo(arguments != null ? arguments: List.of());
        gadgetInfo.setContent(new SceneGadgetInfo.Content.DeshretObeliskGadgetInfo(content));
    }
}
