package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.DungeonEntryData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.enums.LogicType;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DailyDungeonEntryInfoOuterClass.DailyDungeonEntryInfo;
import emu.grasscutter.net.proto.GetDailyDungeonEntryInfoRspOuterClass.GetDailyDungeonEntryInfoRsp;

import java.util.*;

public class PacketGetDailyDungeonEntryInfoRsp extends BasePacket {

    public PacketGetDailyDungeonEntryInfoRsp(Player player, int sceneId) {
        super(PacketOpcodes.GetDailyDungeonEntryInfoRsp);

        this.setData(GetDailyDungeonEntryInfoRsp.newBuilder()
            .addAllDailyDungeonInfoList(GameData.getDungeonEntryDataMap().values().stream()
                .filter(data -> data.getSceneId() == sceneId)
                .filter(DungeonEntryData::isShowInAdvHandbook)
                .filter(data -> isSatisfied(player, data.getCondComb(), data.getSatisfiedCond()))
                .map(data -> getDungeonEntryInfo(player, data))
                .filter(DailyDungeonEntryInfo::hasRecommendDungeonEntryInfo)
                .sorted(Comparator.comparing(DailyDungeonEntryInfo::getDungeonEntryConfigId))
                .toList()));
    }

    private DailyDungeonEntryInfo getDungeonEntryInfo(Player player, DungeonEntryData entryData) {
        DailyDungeonEntryInfo.Builder dailyEntryInfo = DailyDungeonEntryInfo.newBuilder();
        player.getDungeonEntryManager().getDungeonEntries(entryData.getSceneId(), entryData.getDungeonEntryId()).stream()
            .min(Comparator.comparingInt(data -> Math.abs(data.getLimitLevel() - player.getLevel())))
            .ifPresent(dungeonData -> {
                dailyEntryInfo.setRecommendDungeonId(dungeonData.getId());
                dailyEntryInfo.setRecommendDungeonEntryInfo(player.getDungeonEntryManager().toProto(dungeonData));
            });

        return dailyEntryInfo.setDungeonEntryId(entryData.getDungeonEntryId()).setDungeonEntryConfigId(entryData.getId()).build();
    }

    private boolean isSatisfied(Player player, LogicType condComb, List<DungeonEntryData.SatisfiedCond> conds) {
        return LogicType.calculate(condComb, conds.stream()
                .filter(cond -> Optional.ofNullable(cond).map(DungeonEntryData.SatisfiedCond::getType).isPresent())
                .map(cond -> switch (cond.getType()) {
                    case DUNGEON_ENTRY_CONDITION_LEVEL -> player.getLevel() >= cond.getParam1();
                    case DUNGEON_ENTRY_CONDITION_QUEST -> Optional.ofNullable(player.getQuestManager().getQuestById(cond.getParam1()))
                        .map(GameQuest::getState).filter(state -> state == QuestState.QUEST_STATE_FINISHED).isPresent();
                    case DUNGEON_ENTRY_CONDITION_NONE -> true;})
                .mapToInt(result -> result ? 1 : 0)
                .toArray());
    }
}
