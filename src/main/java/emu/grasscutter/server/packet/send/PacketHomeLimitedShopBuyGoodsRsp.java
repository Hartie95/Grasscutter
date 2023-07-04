package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.inventory.MaterialType;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.utils.Utils;
import emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods;
import emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsRspOuterClass.HomeLimitedShopBuyGoodsRsp;
import emu.grasscutter.net.proto.ItemParamOuterClass.ItemParam;

public class PacketHomeLimitedShopBuyGoodsRsp extends BasePacket {

    public PacketHomeLimitedShopBuyGoodsRsp(Player player, HomeLimitedShopGoods good, int buyCount) {
        super(PacketOpcodes.HomeLimitedShopBuyGoodsRsp);
        HomeLimitedShopBuyGoodsRsp.Builder proto = HomeLimitedShopBuyGoodsRsp.newBuilder();

        // if shop is closed but session is not refreshed
        if (Utils.getCurrentSeconds() > player.getHome().getWeeklyDjin().getNextCloseTime()) {
            this.setData(proto.setRetcode(Retcode.RET_SHOP_NOT_OPEN_VALUE).build());
            return;
        }

        // if item reached its buying limit, client might not even send packet in that scenerio, but just to be sure
        if (player.getHome().getWeeklyDjin().getGoods().get(good.getGoodsId()) >= good.getBuyLimit()) {
            this.setData(proto.setRetcode(Retcode.RET_FAIL_VALUE).build());
            return;
        }

        // pay first
        for (ItemParam item : good.getCostItemListList()) {
            if (!player.getInventory().payItem(item.getItemId(), item.getCount())) {
                // TODO add retcode for other pay items
                this.setData(proto.setRetcode(Retcode.RET_HOME_COIN_NOT_ENOUGH_VALUE).build());
                return;
            }
        }

        player.getInventory().addItem(good.getGoodsItem().getItemId(), buyCount, ActionReason.HomeLimitedShopBuy);

        // save to list if it is bgm
        if (GameData.getItemDataMap().get(good.getGoodsItem().getItemId()).getMaterialType() == MaterialType.MATERIAL_BGM) {
            player.getHome().getUnlockedHomeBgmList().add(good.getGoodsItem().getItemId());
        }

        player.getHome().getWeeklyDjin().getGoods().merge(good.getGoodsId(), buyCount, Integer::sum);
        player.getHome().save();
        
        this.setData(proto.setGoods(player.getHome().getWeeklyDjin().goodsToProto(player, good.getGoodsId()))
            .setBuyCount(buyCount).build());
    }
}
