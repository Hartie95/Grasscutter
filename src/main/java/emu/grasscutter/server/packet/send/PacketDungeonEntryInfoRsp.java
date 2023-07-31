package emu.grasscutter.server.packet.send;

import java.util.Comparator;

import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonEntryInfoRspOuterClass.DungeonEntryInfoRsp;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import lombok.val;

public class PacketDungeonEntryInfoRsp extends BasePacket {
    public PacketDungeonEntryInfoRsp(Player player, int sceneId, int pointId) {
        super(PacketOpcodes.DungeonEntryInfoRsp);

        DungeonEntryInfoRsp.Builder proto = DungeonEntryInfoRsp.newBuilder().setPointId(pointId);
        val entries = player.getDungeonEntryManager().getDungeonEntries(sceneId, pointId);
        proto.addAllDungeonEntryList(entries.stream()
            .map(player.getDungeonEntryManager()::toProto)
            .toList());

        entries.stream()
            .min(Comparator.comparingInt(data -> Math.abs(data.getLimitLevel() - player.getLevel())))
            .map(DungeonData::getId)
            .ifPresent(proto::setRecommendDungeonId);

        this.setData(proto.setRetcode(entries.size() > 0 ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE));
    }
}
