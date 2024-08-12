package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.expedition.ExpeditionInfo;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.general.item.ItemParam;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.expedition.AvatarExpeditionGetRewardRsp;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.expedition.AvatarExpeditionInfo;

import java.util.*;

public class PacketAvatarExpeditionGetRewardRsp extends BaseTypedPacket<AvatarExpeditionGetRewardRsp> {
    public PacketAvatarExpeditionGetRewardRsp(Map<Long, ExpeditionInfo> expeditionInfo, Collection<GameItem> items) {
        super(new AvatarExpeditionGetRewardRsp());

        Map<Long, AvatarExpeditionInfo> avatarExpeditionInfoMap = new HashMap<>();
        expeditionInfo.forEach((key, e) -> avatarExpeditionInfoMap.put(key, e.toProto()));
        proto.setExpeditionInfoMap(avatarExpeditionInfoMap);

        List<ItemParam> itemParamList = new ArrayList<>();
        items.forEach(item -> itemParamList.add(item.toItemParam()));
        proto.setItemList(itemParamList);
    }
}
