package emu.grasscutter.game.entity.gadget;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.gadget.chest.BossChestInteractHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.BossChestInfoOuterClass.BossChestInfo;
import emu.grasscutter.net.proto.GadgetInteractReqOuterClass.GadgetInteractReq;
import emu.grasscutter.net.proto.InterOpTypeOuterClass.InterOpType;
import emu.grasscutter.net.proto.InteractTypeOuterClass;
import emu.grasscutter.net.proto.InteractTypeOuterClass.InteractType;
import emu.grasscutter.net.proto.ResinCostTypeOuterClass.ResinCostType;
import emu.grasscutter.net.proto.SceneGadgetInfoOuterClass.SceneGadgetInfo;
import emu.grasscutter.scripts.constants.ScriptGadgetState;
import emu.grasscutter.server.packet.send.PacketGadgetInteractRsp;
import lombok.val;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class GadgetChest extends GadgetContent {

    public GadgetChest(EntityGadget gadget) {
        super(gadget);
    }

    public boolean onInteract(Player player, GadgetInteractReq req) {
        val chestInteractHandlerMap = getGadget().getScene().getWorld().getServer().getWorldDataSystem().getChestInteractHandlerMap();
        val handler = chestInteractHandlerMap.get(getGadget().getGadgetData().getJsonName());
        if (handler == null) {
            Grasscutter.getLogger().warn("Could not found the handler of this type of Chests {}", getGadget().getGadgetData().getJsonName());
            return false;
        }

        if (req.getOpType() == InterOpType.INTER_OP_TYPE_START && handler.isTwoStep()) {
            player.sendPacket(new PacketGadgetInteractRsp(getGadget(), InteractType.INTERACT_TYPE_OPEN_CHEST, InterOpType.INTER_OP_TYPE_START));
            return false;
        } else {
            boolean success;
            if (handler instanceof BossChestInteractHandler bossChestInteractHandler) {
                success = bossChestInteractHandler.onInteract(this, player,
                    req.getResinCostType() == ResinCostType.RESIN_COST_TYPE_CONDENSE);
            } else {
                success = handler.onInteract(this, player);
            }
            if (!success) return false;

            getGadget().updateState(ScriptGadgetState.ChestOpened);
            player.sendPacket(new PacketGadgetInteractRsp(this.getGadget(), InteractTypeOuterClass.InteractType.INTERACT_TYPE_OPEN_CHEST));
            return true;
        }
    }

    /**
     * Builds proto information for gadgets, note that
     * it will override the gadget's properties even if the builder is empty
     * */
    public void onBuildProto(SceneGadgetInfo.Builder gadgetInfo) {
        val playersUid = getGadget().getScene().getPlayers().stream().map(Player::getUid).toList();

        Optional.ofNullable(getGadget().getMetaGadget())
            .map(g -> g.boss_chest)
            .ifPresent(bossChest -> {
                val chestProto = BossChestInfo.newBuilder()
                    .setMonsterConfigId(bossChest.monster_config_id)
                    .setResin(bossChest.resin);

                // removing instead of creating new list directly below is because
                // it also has to consider normal cases
                val qualifiedUids = new ArrayList<>(playersUid);
                // don't allow player to take again if he has taken weekly boss already
                Optional.ofNullable(getGadget().getScene().getDungeonManager())
                    .map(DungeonManager::getWeeklyBossUidInfo).map(chestProto::putAllUidDiscountMap)
                    .map(BossChestInfo.Builder::getUidDiscountMapMap).map(Map::keySet)
                    .ifPresent(qualifiedUids::retainAll);

                gadgetInfo.setBossChest(chestProto
                    .addAllQualifyUidList(playersUid)
                    .addAllRemainUidList(qualifiedUids)
                    .build());
            });

        Optional.ofNullable(getGadget().getScene().getWorld().getOwner().getBlossomManager())
            .map(m -> m.getChestInfo(getGadget().getConfigId(), playersUid))
            .ifPresent(gadgetInfo::setBlossomChest);
    }
}
