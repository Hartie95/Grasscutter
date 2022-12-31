package emu.grasscutter.server.packet.send;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.SceneTagInfoItem;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.PlayerWorldSceneInfoListNotifyOuterClass.PlayerWorldSceneInfoListNotify;
import emu.grasscutter.net.proto.PlayerWorldSceneInfoOuterClass.PlayerWorldSceneInfo;

import java.util.List;

public class PacketPlayerWorldSceneInfoListNotify extends BasePacket {

    public PacketPlayerWorldSceneInfoListNotify(Player player) {
        super(PacketOpcodes.PlayerWorldSceneInfoListNotify);
        player.initSceneTag();

        this.setData(PlayerWorldSceneInfoListNotify.newBuilder()
            .addAllInfoList(player.getSceneTagMap().values().stream()
                .map(SceneTagInfoItem::toProto)
                .toList())
            .build());
    }
}
