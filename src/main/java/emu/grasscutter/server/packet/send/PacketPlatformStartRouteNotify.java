package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.scene.PlatformStartRouteNotify;

public class PacketPlatformStartRouteNotify extends BaseTypedPacket<PlatformStartRouteNotify> {
    public PacketPlatformStartRouteNotify(EntityGadget gadgetEntity) {
        super(new PlatformStartRouteNotify());
        proto.setEntityId(gadgetEntity.getId());
        proto.setPlatform(gadgetEntity.getPlatformInfo());
        proto.setSceneTime(gadgetEntity.getScene().getSceneTime());
    }
}
