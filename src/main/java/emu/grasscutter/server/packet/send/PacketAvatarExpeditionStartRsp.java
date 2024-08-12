package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.expedition.ExpeditionInfo;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.expedition.AvatarExpeditionInfo;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.expedition.AvatarExpeditionStartRsp;

import java.util.HashMap;
import java.util.Map;

public class PacketAvatarExpeditionStartRsp extends BaseTypedPacket<AvatarExpeditionStartRsp> {
    public PacketAvatarExpeditionStartRsp(Map<Long, ExpeditionInfo> expeditionInfo) {
        super(new AvatarExpeditionStartRsp());
        Map<Long, AvatarExpeditionInfo> avatarExpeditionInfoMap = new HashMap<>();
        expeditionInfo.forEach((key, e) -> avatarExpeditionInfoMap.put(key, e.toProto()));
        proto.setExpeditionInfoMap(avatarExpeditionInfoMap);
    }
}
