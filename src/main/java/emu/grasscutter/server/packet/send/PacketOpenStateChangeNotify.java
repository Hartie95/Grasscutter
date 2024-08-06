package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.player.OpenStateChangeNotify;

import java.util.Map;

//Sets openState to value
public class PacketOpenStateChangeNotify extends BaseTypedPacket<OpenStateChangeNotify> {

    public PacketOpenStateChangeNotify(int openState, int value) {
        this(Map.of(openState, value));
    }

    public PacketOpenStateChangeNotify(Map<Integer, Integer> map) {
        super(new OpenStateChangeNotify());
        proto.setOpenStateMap(map);
    }

}
