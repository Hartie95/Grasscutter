package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.dungeon.entry.DungeonEntryInfoRsp;

import java.util.Comparator;

public class PacketDungeonEntryInfoRsp extends BaseTypedPacket<DungeonEntryInfoRsp> {
    public PacketDungeonEntryInfoRsp(Player player, int sceneId, int pointId) {
        super(new DungeonEntryInfoRsp());
        proto.setPointId(pointId);

        val entries = player.getDungeonEntryManager().getDungeonEntries(sceneId, pointId);
        proto.setDungeonEntryList(entries.stream().map(player.getDungeonEntryManager()::toProto).toList());

        entries.stream().min(Comparator.comparingInt(data -> Math.abs(data.getLimitLevel() - player.getLevel())))
            .map(DungeonData::getId).ifPresent(proto::setRecommendDungeonId);

        proto.setRetCode(!entries.isEmpty() ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE);
    }
}
