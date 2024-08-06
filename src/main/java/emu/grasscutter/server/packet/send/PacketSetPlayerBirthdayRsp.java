package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.community.player_presentation.SetPlayerBirthdayRsp;

public class PacketSetPlayerBirthdayRsp extends BaseTypedPacket<SetPlayerBirthdayRsp> {

    public PacketSetPlayerBirthdayRsp(int retCode) {
        super(new SetPlayerBirthdayRsp());
        proto.setRetCode(retCode);
    }

    public PacketSetPlayerBirthdayRsp(Player player) {
        super(new SetPlayerBirthdayRsp());
        proto.setBirthday(player.getBirthday().toProto());
    }
}
