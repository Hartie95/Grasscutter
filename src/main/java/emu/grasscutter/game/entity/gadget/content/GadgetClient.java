package emu.grasscutter.game.entity.gadget.content;

import emu.grasscutter.game.entity.EntityClientGadget;
import emu.grasscutter.game.player.Player;
import lombok.Getter;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.battle.event.EvtCreateGadgetNotify;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.ClientGadgetInfo;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneGadgetInfo;

import java.util.List;

public class GadgetClient extends GadgetContent {

    @Getter private final int ownerEntityId;
    private final int targetEntityId;
    private final List<Integer> targetEntityIds;
    private final boolean asyncLoad;

    @Getter private final int originalOwnerEntityId;
    public GadgetClient(EntityClientGadget gadget) {
        super(gadget);
        val scene = gadget.getScene();
        val createConfig = gadget.getSpawnConfig();
        this.targetEntityId = createConfig.getTargetEntityId();
        this.targetEntityIds = createConfig.getTargetEntityIds();
        if(createConfig.getInitDataSource() instanceof EvtCreateGadgetNotify notify){
            this.ownerEntityId = notify.getPropOwnerEntityId();
            this.asyncLoad = notify.isAsyncLoad();
        } else {
            this.ownerEntityId = createConfig.getOwner().getId();
            this.asyncLoad = false;
        }


        val owner = scene.getEntityById(this.ownerEntityId);
        if (owner instanceof EntityClientGadget ownerGadget) {
            this.originalOwnerEntityId = ownerGadget.getOriginalOwnerEntityId();
        } else {
            this.originalOwnerEntityId = this.ownerEntityId;
        }
    }

    @Override
    public boolean onInteract(Player player, GadgetInteractReq req) {
        return false;
    }

    @Override
    public void onBuildProto(SceneGadgetInfo gadgetInfo) {
        val clientGadget = new ClientGadgetInfo(getGadget().getCampId(),getGadget().getCampType());
        clientGadget.setOwnerEntityId(this.ownerEntityId);
        clientGadget.setTargetEntityId(this.targetEntityId);
        clientGadget.setTargetEntityIdList(this.targetEntityIds);
        clientGadget.setAsyncLoad(this.asyncLoad);
        gadgetInfo.setContent(new SceneGadgetInfo.Content.ClientGadget(clientGadget));
        gadgetInfo.setOwnerEntityId(this.ownerEntityId);
        gadgetInfo.setPropOwnerEntityId(this.ownerEntityId);
    }
}
