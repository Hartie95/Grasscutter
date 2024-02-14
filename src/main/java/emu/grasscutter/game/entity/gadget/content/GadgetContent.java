package emu.grasscutter.game.entity.gadget.content;

import emu.grasscutter.game.entity.EntityBaseGadget;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.player.Player;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneGadgetInfo;

public abstract class GadgetContent {
	private final EntityBaseGadget gadget;

	public GadgetContent(EntityBaseGadget gadget) {
		this.gadget = gadget;
	}

	public EntityBaseGadget getGadget() {
		return gadget;
	}

	public abstract boolean onInteract(Player player, GadgetInteractReq req);

	public abstract void onBuildProto(SceneGadgetInfo gadgetInfo);
}
