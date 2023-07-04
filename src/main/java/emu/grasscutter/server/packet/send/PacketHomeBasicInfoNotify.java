package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.home.HomeSceneItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeBasicInfoNotifyOuterClass.HomeBasicInfoNotify;
import emu.grasscutter.net.proto.HomeBasicInfoOuterClass.HomeBasicInfo;
import emu.grasscutter.net.proto.VectorOuterClass;

public class PacketHomeBasicInfoNotify extends BasePacket {

    public PacketHomeBasicInfoNotify(Player player, boolean editMode) {
        super(PacketOpcodes.HomeBasicInfoNotify);

        if (player.getCurrentRealmId() <= 0) return;

        HomeBasicInfoNotify.Builder proto = HomeBasicInfoNotify.newBuilder();

        HomeSceneItem homeScene = player.getHome().getHomeSceneItem();

        player.getHome().updateWeeklyDjin();
        proto.setBasicInfo(HomeBasicInfo.newBuilder()
                .setCurModuleId(player.getCurrentRealmId())
                .setCurRoomSceneId(homeScene.getRoomSceneId())
                .setIsInEditMode(editMode)
                .setHomeOwnerUid(player.getUid())
                .setLevel(player.getHome().getLevel())
                .setOwnerNickName(player.getNickname())
                .setExp(player.getHome().getExp())
                .setLimitedShopInfo(player.getHome().getWeeklyDjin().toProtoBuilder()
                    .setUid(player.getUid())
                    .build())
                .build());

        this.setData(proto);
    }
}
