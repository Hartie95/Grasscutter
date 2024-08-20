package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.general.item.ItemParam;
import org.anime_game_servers.multi_proto.gi.messages.home.FurnitureMakeData;
import org.anime_game_servers.multi_proto.gi.messages.home.FurnitureMakeSlot;
import org.anime_game_servers.multi_proto.gi.messages.home.TakeFurnitureMakeRsp;

import java.util.List;

public class PacketTakeFurnitureMakeRsp extends BaseTypedPacket<TakeFurnitureMakeRsp> {
    public PacketTakeFurnitureMakeRsp(int ret,
                                      int makeId,
                                      List<ItemParam> output,
                                      List<FurnitureMakeData> others) {
        super(new TakeFurnitureMakeRsp());
        proto.setRetcode(ret);
        proto.setMakeId(makeId);

        if (output != null) {
            proto.setOutputItemList(output);
        }

        if (others != null) {
            val furnitureMakeSlot = new FurnitureMakeSlot();
            furnitureMakeSlot.setFurnitureMakeDataList(others);
            proto.setFurnitureMakeSlot(furnitureMakeSlot);
        }
    }
}
