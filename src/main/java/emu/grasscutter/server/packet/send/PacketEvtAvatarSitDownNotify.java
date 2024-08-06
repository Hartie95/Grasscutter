package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.battle.event.EvtAvatarSitDownNotify;

public class PacketEvtAvatarSitDownNotify extends BaseTypedPacket<EvtAvatarSitDownNotify> {

    public PacketEvtAvatarSitDownNotify(EvtAvatarSitDownNotify notify) {
        super(new EvtAvatarSitDownNotify());
        proto.setEntityId(notify.getEntityId());
        proto.setPosition(notify.getPosition());
        proto.setChairId(notify.getChairId());
    }
}
