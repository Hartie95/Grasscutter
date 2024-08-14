package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.item.management.ReliquaryDecomposeRsp;

import java.util.List;

public class PacketReliquaryDecomposeRsp extends BaseTypedPacket<ReliquaryDecomposeRsp> {
	public PacketReliquaryDecomposeRsp(Retcode retcode) {
        super(new ReliquaryDecomposeRsp());
        proto.setRetCode(retcode.getNumber());
	}

	public PacketReliquaryDecomposeRsp(List<Long> output) {
        super(new ReliquaryDecomposeRsp());
        proto.setGuidList(output);
	}
}
