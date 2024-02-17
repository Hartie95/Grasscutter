package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.DungeonDataNotify;
import org.anime_game_servers.game_data_models.gi.data.dungeon.DungeonData;

public class PacketDungeonDataNotify extends BaseTypedPacket<DungeonDataNotify> {
    public PacketDungeonDataNotify(DungeonData dungeonData) {
        super(new DungeonDataNotify());
        // TODO
    }
}
