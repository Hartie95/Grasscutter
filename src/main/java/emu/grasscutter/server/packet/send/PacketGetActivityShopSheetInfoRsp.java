package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.activity.ActivityConfigItem;
import emu.grasscutter.game.activity.ActivityManager;
import emu.grasscutter.net.packet.BaseTypedPackage;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import emu.grasscutter.server.game.GameSession;
import messages.shop.ActivityShopSheetInfo;
import messages.shop.GetActivityShopSheetInfoRsp;

import java.util.ArrayList;
import java.util.List;

public class PacketGetActivityShopSheetInfoRsp extends BaseTypedPackage<GetActivityShopSheetInfoRsp> {


    public PacketGetActivityShopSheetInfoRsp(int shopType) {
        super(new GetActivityShopSheetInfoRsp());

        var sheetInfo = GameData.getActivityShopDataMap().get(shopType);
        ActivityConfigItem activityConfigItem = null;

        if (sheetInfo != null) {
            activityConfigItem = ActivityManager.getScheduleActivityConfigMap().get(sheetInfo.getScheduleId());
        }

        proto.setShopType(shopType);

        if (sheetInfo == null || activityConfigItem == null) {
            proto.setRetcode(RetcodeOuterClass.Retcode.RET_SHOP_NOT_OPEN_VALUE);
            return;
        }

        List<ActivityShopSheetInfo> sheetInfos = new ArrayList<>(sheetInfo.getSheetList().size());
        for (int id : sheetInfo.getSheetList()) {
            sheetInfos.add(new ActivityShopSheetInfo(
                id,
                (int)activityConfigItem.getBeginTime().getTime(),
                (int)activityConfigItem.getEndTime().getTime()));
        }
        proto.setSheetInfoList(sheetInfos);
    }
}
