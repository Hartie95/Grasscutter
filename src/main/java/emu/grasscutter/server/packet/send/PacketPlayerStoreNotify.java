package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.storage.PlayerStoreNotify;
import org.anime_game_servers.multi_proto.gi.messages.storage.StoreType;

import static emu.grasscutter.config.Configuration.GAME_OPTIONS;

public class PacketPlayerStoreNotify extends BaseTypedPacket<PlayerStoreNotify> {

    public PacketPlayerStoreNotify(Player player) {
        super(new PlayerStoreNotify());
        this.buildHeader(2);
        proto.setStoreType(StoreType.STORE_PACK);
        proto.setWeightLimit(GAME_OPTIONS.inventoryLimits.all);
        proto.setItemList(player.getInventory().stream().map(GameItem::toProto).toList());
    }
}
