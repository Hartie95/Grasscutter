package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.home.*;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomePlantFieldDataOuterClass.HomePlantFieldData;
import emu.grasscutter.net.proto.HomePlantFieldNotifyOuterClass.HomePlantFieldNotify;

import java.util.List;

public class PacketHomePlantFieldNotify extends BasePacket {

    public PacketHomePlantFieldNotify(Player player, List<Integer> seedIdList, int fieldGuid, int plantIndex) {
        super(PacketOpcodes.HomePlantFieldNotify);
        HomePlantFieldNotify.Builder proto = HomePlantFieldNotify.newBuilder();
        HomePlantFieldItem fieldItem = player.getHome().getHomePlantItem().getFieldByGuid(fieldGuid);
        if (seedIdList.size() == 0 || fieldItem == null) return; // if requested plant seed is empty or selected plant block not existed

        // if not all seed planted, dont save or delete seed from inventory, however it might show 
        // seed planted during the session
        if (!fieldItem.setAllSubFieldProperties(plantIndex, seedIdList)) return;

        seedIdList.stream().forEach(seedId -> {
            player.getInventory().payItem(seedId, 1);
        });

        player.getHome().save();
        this.setData(proto.setField(fieldItem.toProto()).build());
    }

    public PacketHomePlantFieldNotify(Player player, int fieldGuid) {
        super(PacketOpcodes.HomePlantFieldNotify);
        HomePlantFieldItem fieldItem = player.getHome().getHomePlantItem().getFieldByGuid(fieldGuid);
        if (fieldItem == null) return;

        this.setData(HomePlantFieldNotify.newBuilder().setField(fieldItem.toProto()).build());
    }
}
