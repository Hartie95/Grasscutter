package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomePlantInfoNotifyOuterClass.HomePlantInfoNotify;

import java.util.List;

public class PacketHomePlantInfoNotify extends BasePacket {

    public PacketHomePlantInfoNotify(Player player) {
        super(PacketOpcodes.HomePlantInfoNotify);
        if (player.getCurrentRealmId() <= 0) {
            return;
        }

        HomePlantInfoNotify.Builder proto = HomePlantInfoNotify.newBuilder();
        if (player.getHome().getRealmPlantMap().get(player.getCurrentRealmId()) != null) {
            player.getHome().getRealmPlantMap().get(player.getCurrentRealmId()).getFieldList().forEach(
                plantFieldItem -> proto.addFieldList(plantFieldItem.toProto())
            );
        }
        this.setData(proto.build());
    }
}
