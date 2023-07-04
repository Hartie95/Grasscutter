package emu.grasscutter.game.managers;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.FurnitureMakeConfigData;
import emu.grasscutter.game.home.FurnitureMakeSlotItem;
import emu.grasscutter.game.player.BasePlayerManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.net.proto.ItemParamOuterClass;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.packet.send.*;
import emu.grasscutter.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class FurnitureManager extends BasePlayerManager {

    public FurnitureManager(Player player) {
        super(player);
    }

    public void onLogin() {
        notifyUnlockFurniture();
        notifyUnlockFurnitureSuite();
    }

    public void notifyUnlockFurniture() {
        player.getSession().send(new PacketUnlockedFurnitureFormulaDataNotify(player.getUnlockedFurniture()));
    }

    public void notifyUnlockFurnitureSuite() {
        player.getSession().send(new PacketUnlockedFurnitureSuiteDataNotify(player.getUnlockedFurnitureSuite()));
    }

    public boolean unlockFurnitureFormula(int id) {
        if (!player.getUnlockedFurniture().add(id)) {
            return false;  // Already unlocked!
        }
        notifyUnlockFurniture();
        return true;
    }

    public boolean unlockFurnitureSuite(int id) {
        if (!player.getUnlockedFurnitureSuite().add(id)) {
            return false;  // Already unlocked!
        }
        notifyUnlockFurnitureSuite();
        return true;
    }

    public void startMake(int makeId, int avatarId) {
        FurnitureMakeConfigData makeData = GameData.getFurnitureMakeConfigDataMap().get(makeId);
        if (makeData == null) {
            player.getSession().send(new PacketFurnitureMakeStartRsp(Retcode.RET_FURNITURE_MAKE_CONFIG_ERROR_VALUE, null));
            return;
        }

        // check slot count
        if (player.getHome().getLevelData().getFurnitureMakeSlotCount() <= player.getHome().getFurnitureMakeSlotItemList().size()) {
            player.getSession().send(new PacketFurnitureMakeStartRsp(Retcode.RET_FURNITURE_MAKE_SLOT_FULL_VALUE, null));
            return;
        }

        // pay items first
        if (!player.getInventory().payItems(makeData.getMaterialItems(), 1, ActionReason.FurnitureMakeStart)) {
            player.getSession().send(new PacketFurnitureMakeStartRsp(Retcode.RET_HOME_FURNITURE_COUNT_NOT_ENOUGH_VALUE, null));
            return;
        }

        // add furniture make task
        player.getHome().getFurnitureMakeSlotItemList().add(FurnitureMakeSlotItem.of()
            .index(player.getHome().getLastMakingSlot() + 1)
            .avatarId(avatarId)
            .makeId(makeId)
            .beginTime(Utils.getCurrentSeconds())
            .durTime(makeData.getMakeTime())
            .build());

        player.getSession().send(new PacketFurnitureMakeStartRsp(Retcode.RET_SUCC_VALUE,
            player.getHome().getFurnitureMakeSlotItemList().stream()
                .map(FurnitureMakeSlotItem::toProto)
                .toList()));

        player.getHome().save();
    }

    public void queryStatus() {
        if (player.getHome().getFurnitureMakeSlotItemList() == null) {
            player.getHome().setFurnitureMakeSlotItemList(new ArrayList<>());
        }

        player.sendPacket(new PacketFurnitureMakeRsp(player));
    }

    public void take(int index, int makeId, boolean isFastFinish) {
        FurnitureMakeConfigData makeData = GameData.getFurnitureMakeConfigDataMap().get(makeId);
        if (makeData == null) {
            player.getSession().send(new PacketTakeFurnitureMakeRsp(Retcode.RET_FURNITURE_MAKE_CONFIG_ERROR_VALUE, makeId, null, null));
            return;
        }

        FurnitureMakeSlotItem slotItem = player.getHome().getFurnitureMakeSlotItemList().stream()
            .filter(x -> x.getIndex() == index && x.getMakeId() == makeId)
            .findFirst()
            .orElse(null);

        if (slotItem == null) {
            player.getSession().send(new PacketTakeFurnitureMakeRsp(Retcode.RET_FURNITURE_MAKE_NO_MAKE_DATA_VALUE, makeId, null, null));
            return;
        }

        // pay the speedup item
        if (isFastFinish && !player.getInventory().payItem(107013,1)) {
            player.getSession().send(new PacketTakeFurnitureMakeRsp(Retcode.RET_FURNITURE_MAKE_UNFINISH_VALUE, makeId, null, null));
            return;
        }

        // check if player can take
        // if (slotItem.get().getBeginTime() + slotItem.get().getDurTime() >= Utils.getCurrentSeconds() && !isFastFinish) {
        //    player.getSession().send(new PacketTakeFurnitureMakeRsp(Retcode.RET_FURNITURE_MAKE_UNFINISH_VALUE, makeId, null, null));
        //    return;
        // }

        player.getInventory().addItem(
            makeData.getFurnitureItemID(), 
            makeData.getCount(), 
            isFastFinish ? ActionReason.FurnitureMakeFastFinish : ActionReason.FurnitureMakeTake
        );
            
        // add home exp if first time crafting furniture
        if (player.getHome().furnitureShouldGiveExp(makeData.getFurnitureItemID())) {
            player.getHome().addExp(makeData.getExp());
            player.sendPacket(new PacketHomeBasicInfoNotify(player, false));
        }

        player.sendPacket(new PacketFunitureMakeMakeInfoChangeNotify(
            makeData.getFurnitureItemID(), 
            player.getMadeFurniture().get(makeData.getFurnitureItemID())
        ));
        
        player.getHome().getFurnitureMakeSlotItemList().remove(slotItem);
        player.getHome().updateMakingSlotIndex(index);

        player.getSession().send(new PacketTakeFurnitureMakeRsp(Retcode.RET_SUCC_VALUE, makeId,
            List.of(ItemParamOuterClass.ItemParam.newBuilder()
                .setItemId(makeData.getFurnitureItemID())
                .setCount(makeData.getCount())
            .build()),
            player.getHome().getFurnitureMakeSlotItemList().stream()
                .map(FurnitureMakeSlotItem::toProto)
                .toList()
        ));

        player.getHome().save();
    }

    public void cancelMake(int index, int makeId) {
        FurnitureMakeConfigData makeData = GameData.getFurnitureMakeConfigDataMap().get(makeId);
        if (makeData == null) {
            player.sendPacket(new PacketFurnitureMakeCancelRsp(Retcode.RET_FURNITURE_MAKE_CONFIG_ERROR, makeId, null));
            return;
        }

        FurnitureMakeSlotItem slotItem = player.getHome().getFurnitureMakeSlotItemList().stream()
            .filter(x -> x.getIndex() == index && x.getMakeId() == makeId)
            .findFirst()
            .orElse(null);

        if (slotItem == null) {
            player.sendPacket(new PacketFurnitureMakeCancelRsp(Retcode.RET_FURNITURE_MAKE_NO_MAKE_DATA, makeId, null));
            return;
        }
        player.getInventory().addItemParamDatas(makeData.getMaterialItems(), ActionReason.FurnitureMakeCancel);
        player.getHome().getFurnitureMakeSlotItemList().remove(slotItem);
        player.getHome().updateMakingSlotIndex(index);
        player.sendPacket(new PacketFurnitureMakeCancelRsp(Retcode.RET_SUCC, makeId, player.getHome().getFurnitureMakeSlotItemList()));
        player.getHome().save();
    }
}
