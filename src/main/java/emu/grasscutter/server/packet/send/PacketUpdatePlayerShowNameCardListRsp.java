package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.community.player_presentation.UpdatePlayerShowNameCardListRsp;

import java.util.List;

public class PacketUpdatePlayerShowNameCardListRsp extends BaseTypedPacket<UpdatePlayerShowNameCardListRsp> {
    public PacketUpdatePlayerShowNameCardListRsp(List<Integer> cardIds) {
        super(new UpdatePlayerShowNameCardListRsp());
        proto.setShowNameCardIdList(cardIds);
        proto.setRetcode(0);
    }
}
