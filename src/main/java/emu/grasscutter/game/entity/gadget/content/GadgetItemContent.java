package emu.grasscutter.game.entity.gadget.content;

import emu.grasscutter.game.entity.EntityBaseGadget;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.server.packet.send.PacketGadgetInteractRsp;
import lombok.Getter;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.gadget.InteractType;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneGadgetInfo;

public class GadgetItemContent extends GadgetContent {
    private final GameItem item;
    @Getter private final long guid;
    @Getter private final boolean share;
    public GadgetItemContent(EntityBaseGadget gadget) {
        super(gadget);
        val config = gadget.getSpawnConfig();
        this.item = config.getItem();
        val player = config.getPlayerOwner();
        val scene = gadget.getScene();
        this.guid = player == null ? scene.getWorld().getHost().getNextGameGuid() : player.getNextGameGuid();
        this.share = config.isShareItem();
    }

    @Override
    public boolean onInteract(Player player, GadgetInteractReq req) {
        // check drop owner to avoid someone picked up item in others' world
        if (!this.isShare()) {
            int dropOwner = (int) (this.getGuid() >> 32);
            if (dropOwner != player.getUid()) {
                return false;
            }
        }

        val scene = getGadget().getScene();

        // Add to inventory
        boolean success = player.getInventory().addItem(item, ActionReason.SubfieldDrop);
        if (success) {
            if (!this.isShare()) { // not shared drop
                player.sendPacket(new PacketGadgetInteractRsp(getGadget(), InteractType.INTERACT_PICK_ITEM));
            } else {
                scene.broadcastPacket(new PacketGadgetInteractRsp(getGadget(), InteractType.INTERACT_PICK_ITEM));
            }
        }
        return success;
    }

    @Override
    public void onBuildProto(SceneGadgetInfo gadgetInfo) {
        gadgetInfo.setContent(new SceneGadgetInfo.Content.TrifleItem(item.toProto()));
    }
}
