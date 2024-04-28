package emu.grasscutter.game.entity.gadget;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.CodexViewpointData;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketCodexDataUpdateNotify;
import messages.gadget.GadgetInteractReq;
import messages.scene.entity.SceneGadgetInfo;


public class GadgetViewPoint extends  GadgetContent{


    public GadgetViewPoint(EntityGadget gadget) {
        super(gadget);
    }

    @Override
    public boolean onInteract(Player player, GadgetInteractReq req) {
        var groupId = this.getGadget().getGroupId();
        var configId = this.getGadget().getConfigId();
        CodexViewpointData viewPoint = GameData.getViewCodexByGroupdCfg(groupId, configId);

        if(viewPoint != null){
            player.getScene().broadcastPacket(new PacketCodexDataUpdateNotify(7,viewPoint.getId()));
            return true;
        }

        return false;
    }

    @Override
    public void onBuildProto(SceneGadgetInfo gadgetInfo) {

    }
}
