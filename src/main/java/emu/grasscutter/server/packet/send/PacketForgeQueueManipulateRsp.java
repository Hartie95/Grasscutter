package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.general.item.ItemParam;
import org.anime_game_servers.multi_proto.gi.messages.item.forge.ForgeQueueManipulateRsp;
import org.anime_game_servers.multi_proto.gi.messages.item.forge.ForgeQueueManipulateType;

import java.util.List;

public class PacketForgeQueueManipulateRsp extends BaseTypedPacket<ForgeQueueManipulateRsp> {
    public PacketForgeQueueManipulateRsp(Retcode retcode, ForgeQueueManipulateType type, List<GameItem> output, List<GameItem> refund, List<GameItem> extra) {
        super(new ForgeQueueManipulateRsp());
        proto.setRetCode(retcode.getNumber());
        proto.setManipulateType(type);
        proto.setOutputItemList(gameItemToItemParam(output));
        proto.setReturnItemList(gameItemToItemParam(refund));
        // ToDo: Add extra items when once we have handling for it.
    }

    private List<ItemParam> gameItemToItemParam(List<GameItem> input) {
        return input.stream()
            .map(item -> {
                ItemParam toAdd = new ItemParam();
                toAdd.setItemId(item.getItemId());
                toAdd.setCount(item.getCount());
                return toAdd;
            }).toList();
    }
}
