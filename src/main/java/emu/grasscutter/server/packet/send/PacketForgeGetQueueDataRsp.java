package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.item.forge.ForgeGetQueueDataRsp;
import org.anime_game_servers.multi_proto.gi.messages.item.forge.ForgeQueueData;

import java.util.Map;

public class PacketForgeGetQueueDataRsp extends BaseTypedPacket<ForgeGetQueueDataRsp> {
	public PacketForgeGetQueueDataRsp(Retcode retcode, int numQueues, Map<Integer, ForgeQueueData> queueData) {
        super(new ForgeGetQueueDataRsp());
        proto.setRetCode(retcode.getNumber());
        proto.setMaxQueueNum(numQueues);
        proto.setForgeQueueMap(queueData);
	}
}
