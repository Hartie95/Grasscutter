package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.home.HomeBlockItem;
import emu.grasscutter.game.home.HomeSceneItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeComfortInfoNotifyOuterClass.HomeComfortInfoNotify;
import emu.grasscutter.net.proto.HomeModuleComfortInfoOuterClass.HomeModuleComfortInfo;

import java.util.ArrayList;
import java.util.List;

public class PacketHomeComfortInfoNotify extends BasePacket {

    public PacketHomeComfortInfoNotify(Player player) {
        super(PacketOpcodes.HomeComfortInfoNotify);

        if (player.getRealmList() == null) {
            // Do not send
            return;
        }

        List<HomeModuleComfortInfo> comfortInfoList = new ArrayList<>();

        int highestComfort = 0;
        for (int moduleId : player.getRealmList()) {
            HomeSceneItem homeScene = player.getHome().getHomeSceneItem(moduleId, player.getHome().getRealmSceneIdByModule(moduleId));

            List<Integer> blockComfortList = homeScene.getBlockItems().values().stream()
                .map(HomeBlockItem::calComfort)
                .toList();

            HomeSceneItem roomScene = player.getHome().getHomeSceneItem(moduleId, homeScene.getRoomSceneId());

            comfortInfoList.add(HomeModuleComfortInfo.newBuilder()
                .setModuleId(moduleId)
                .setRoomSceneComfortValue(roomScene.calComfort())
                .addAllWorldSceneBlockComfortValueList(blockComfortList)
                .build());

            int currentRealmTotalComfort = blockComfortList.stream().mapToInt(Integer::intValue).sum() + roomScene.calComfort();
            highestComfort = highestComfort > currentRealmTotalComfort ? highestComfort : currentRealmTotalComfort;
        }

        player.getHome().setHighestComfort(highestComfort);

        HomeComfortInfoNotify proto = HomeComfortInfoNotify.newBuilder()
                .addAllModuleInfoList(comfortInfoList)
                .build();

        this.setData(proto);
    }
}
