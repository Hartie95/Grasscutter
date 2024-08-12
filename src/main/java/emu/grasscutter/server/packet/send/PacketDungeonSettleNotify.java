package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.dungeons.dungeon_results.BaseDungeonResult;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.progression.DungeonSettleNotify;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.progression.ParamList;

import java.util.HashMap;
import java.util.Map;

public class PacketDungeonSettleNotify extends BaseTypedPacket<DungeonSettleNotify> {
    public PacketDungeonSettleNotify(BaseDungeonResult result) {
        super(new DungeonSettleNotify());
        proto.setUseTime(result.getProto().getUseTime());
        proto.setDungeonId(result.getProto().getDungeonId());
        proto.setSuccess(result.getProto().getIsSuccess());
        proto.setCloseTime(result.getProto().getCloseTime());
        proto.setResult(result.getProto().getResult());
        proto.setCreatePlayerUid(result.getProto().getCreatePlayerUid());
        Map<Integer, ParamList> settleShowMap = new HashMap<>();
        result.getProto().getSettleShowMap().keySet().forEach(
            k -> settleShowMap.put(k, new ParamList(result.getProto().getSettleShowMap().get(k).getParamListList())));
        proto.setSettleShow(settleShowMap);
	}
}
