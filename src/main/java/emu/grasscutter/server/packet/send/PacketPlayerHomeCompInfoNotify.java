package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.PlayerHomeCompInfoNotifyOuterClass.PlayerHomeCompInfoNotify;
import emu.grasscutter.net.proto.PlayerHomeCompInfoOuterClass.PlayerHomeCompInfo;

import java.util.List;

public class PacketPlayerHomeCompInfoNotify extends BasePacket {

    public PacketPlayerHomeCompInfoNotify(Player player) {
        super(PacketOpcodes.PlayerHomeCompInfoNotify);

        if (player.getRealmList() == null) {
            // Do not send
            return;
        }

        PlayerHomeCompInfo.Builder homeCompInfoProto = PlayerHomeCompInfo.newBuilder()
            .addAllUnlockedModuleIdList(player.getRealmList())
            .setFriendEnterHomeOptionValue(player.getHome().getEnterHomeOption())
            .addAllLevelupRewardGotLevelList(player.getTeapotLevelReward().stream().toList());
            
        PlayerHomeCompInfoNotify proto = PlayerHomeCompInfoNotify.newBuilder()
            .setCompInfo(homeCompInfoProto.build())
            .build();

        this.setData(proto);
    }
}
