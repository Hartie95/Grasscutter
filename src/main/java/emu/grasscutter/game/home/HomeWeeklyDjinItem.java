package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Transient;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.HomeWorldLimitShopData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.proto.HomeBlockFieldDataOuterClass.HomeBlockFieldData;
import emu.grasscutter.net.proto.HomeLimitedShopInfoOuterClass.HomeLimitedShopInfo;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.HomeLimitedShopOuterClass.HomeLimitedShop;
import emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods;
import emu.grasscutter.net.proto.ItemParamOuterClass.ItemParam;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class HomeWeeklyDjinItem {
    int appearBlock;
    int nextOpenTime;
    int nextCloseTime;
    int nextGuestOpenTime;
    // 1st integer goods Id, 2nd integer bought count
    ConcurrentHashMap<Integer, Integer> goods;

    @Transient Position pos;
    @Transient Position rot;

    public static HomeWeeklyDjinItem create() {
        return HomeWeeklyDjinItem.of()
            .appearBlock(1)
            .nextOpenTime(0)
            .nextCloseTime(0)
            .nextGuestOpenTime(0)
            .goods(new ConcurrentHashMap<Integer, Integer>())
            .build();
    } 

    public void updatePos(int appearBlock, Position pos, Position rot) {
        this.appearBlock = appearBlock;
        this.pos = pos;
        this.rot = rot;
    }

    public int setTime(boolean isNext, DayOfWeek day) {
        TemporalAdjuster adjustTime = isNext ? TemporalAdjusters.next(day) : TemporalAdjusters.previous(day);
        return (int) LocalDate.now().with(adjustTime).atTime(4,0).toEpochSecond(ZonedDateTime.now().getOffset());
    }

    public void setOpenTime(boolean isNext) {
        this.nextOpenTime = setTime(isNext, DayOfWeek.FRIDAY);
    }

    public void setCloseTime(boolean isNext) {
        this.nextCloseTime = setTime(isNext, DayOfWeek.MONDAY);
    }

    public void setGuestOpenTime(boolean isNext) {
        this.nextGuestOpenTime = setTime(isNext, DayOfWeek.SATURDAY);
    }

    public void setAllTime(boolean closeTimeIsNext, boolean openTimeIsNext, boolean guestOpenTimeIsNext) {
        setCloseTime(closeTimeIsNext);
        setOpenTime(openTimeIsNext);
        setGuestOpenTime(guestOpenTimeIsNext);
    }

    public HomeLimitedShopInfo.Builder toProtoBuilder() {
        return HomeLimitedShopInfo.newBuilder()
            .setNextOpenTime(getNextOpenTime())
            .setNextCloseTime(getNextCloseTime())
            .setNextGuestOpenTime(getNextGuestOpenTime())
            .setDjinnPos(getPos().toProto())
            .setDjinnRot(getRot().toProto());
    }

    public HomeWorldLimitShopData getGoodsDataById(int id) {
        return GameData.getHomeWorldLimitShopDataMap().values().stream()
            .filter(x -> x.getGoodsId() == id)
            .findAny()
            .orElse(null);
    }

    public HomeLimitedShopGoods.Builder goodsToProto(Player player, int goodsId) {
        HomeLimitedShopGoods.Builder goodsProto = HomeLimitedShopGoods.newBuilder();
        HomeWorldLimitShopData goodsData = getGoodsDataById(goodsId);
        if (goodsData == null) return goodsProto;

        // disable type 
        // 0 -> none
        // 4 -> some item only limits to bought once, like bgm
        
        goodsProto.setBuyLimit(goodsData.getBuyLimit())
            .setBoughtNum(getGoods().get(goodsId))
            .setGoodsId(goodsData.getGoodsId())
            .setDisableType(player.getHome().getUnlockedHomeBgmList().contains(goodsData.getItemID()) ? 4 : 0)
            .setGoodsItem(ItemParam.newBuilder()
                .setItemId(goodsData.getItemID())
                .setCount(1)
                .build());

        goodsData.getCostItems().stream().forEach(item -> {
            goodsProto.addCostItemList(ItemParam.newBuilder()
                .setItemId(item.getId())
                .setCount(item.getCount())
                .build());
        });
        return goodsProto;
    }

    public HomeLimitedShop shopToProto(Player player) {
        HomeLimitedShop.Builder shopProto = HomeLimitedShop.newBuilder();

        goods.entrySet().stream().forEach(
            e -> shopProto.addGoodsList(goodsToProto(player, e.getKey()).build())
        );

        return shopProto.build();
    }
}
