package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.EntityTagChangeNotify;

public class PacketEntityTagChangeNotify extends BaseTypedPacket<EntityTagChangeNotify> {
    public PacketEntityTagChangeNotify(int entityId, boolean isAdd, String tag) {
        super(new EntityTagChangeNotify());
        proto.setEntityId(entityId);
        proto.setAdd(isAdd);
        proto.setTag(tag);
    }
}
