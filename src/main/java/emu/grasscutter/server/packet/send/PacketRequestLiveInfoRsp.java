package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.gadget.RequestLiveInfoRsp;

public class PacketRequestLiveInfoRsp extends BaseTypedPacket<RequestLiveInfoRsp> {

	public PacketRequestLiveInfoRsp(int live_id, String live_url) {
        super(new RequestLiveInfoRsp());
        proto.setLiveId(live_id);
        proto.setLiveUrl(live_url);
        proto.setRetCode(0);
	}
}
