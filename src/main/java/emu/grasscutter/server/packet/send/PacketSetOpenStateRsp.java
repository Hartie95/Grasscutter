package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.player.SetOpenStateRsp;

public class PacketSetOpenStateRsp extends BaseTypedPacket<SetOpenStateRsp> {
    public PacketSetOpenStateRsp(int openState, int value) {
        super(new SetOpenStateRsp());
        proto.setKey(openState);
        proto.setValue(value);
    }

    public PacketSetOpenStateRsp(Retcode retcode) {
        super(new SetOpenStateRsp());
        proto.setRetcode(retcode.getNumber());
    }
}
