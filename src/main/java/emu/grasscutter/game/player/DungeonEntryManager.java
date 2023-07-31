package emu.grasscutter.game.player;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScenePointEntry;
import emu.grasscutter.data.common.PointData;
import emu.grasscutter.data.common.quest.SubQuestData;
import emu.grasscutter.data.excels.DungeonData;
import emu.grasscutter.data.excels.DungeonRosterData;
import emu.grasscutter.data.excels.DungeonSerialData;
import emu.grasscutter.game.quest.GameMainQuest;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.net.proto.DungeonEntryInfoOuterClass.DungeonEntryInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.stream.Stream;

public class DungeonEntryManager extends BasePlayerManager {
    /**
     * entry: [(sceneId << 16) + pointId, dungeonId]
     * */
    private final Map<Integer, List<Integer>> plotDungeonEntries = new Int2ObjectOpenHashMap<>();

    public DungeonEntryManager(@NonNull Player player) {
        super(player);
    }

    public void onLogin() {
        // find out any dungeon entries for specific quest
        this.player.getQuestManager().getMainQuests().values().parallelStream()
            .map(GameMainQuest::getChildQuests).map(Map::values).flatMap(Collection::stream)
            .filter(quest -> quest.getState() == QuestState.QUEST_STATE_UNFINISHED)
            .map(GameQuest::getQuestData)
            .forEach(data -> Optional.ofNullable(data.getGuide()).map(SubQuestData.Guide::getGuideScene)
                .ifPresent(sceneId -> data.getFinishCond().stream()
                    .filter(c -> c.getType() == QuestContent.QUEST_CONTENT_ENTER_DUNGEON)
                    .forEach(cond -> this.plotDungeonEntries.computeIfAbsent(
                        (sceneId << 16) + cond.getParam()[1], f -> new ArrayList<>()).add(cond.getParam()[0]))));

        if (Optional.ofNullable(this.player.getDungeonEntryItem().getBossRecordMap())
            .map(Map::keySet).filter(keys -> keys.containsAll(GameData.getDungeonSerialDataMap().keySet())).isEmpty()) {
            GameData.getDungeonDataMap().values().parallelStream().filter(data -> data.getSerialId() > 0)
                .map(data -> GameData.getDungeonSerialDataMap().get(data.getSerialId())).filter(Objects::nonNull)
                .forEach(serialData -> this.player.getDungeonEntryItem().getBossRecordMap()
                    .putIfAbsent(serialData.getId(), DungeonEntryItem.WeeklyBossRecord.create(serialData)));
            this.player.save(); // TODO check if this is causing problem or anything
        }
        this.player.getDungeonEntryItem().resetWeeklyBoss();
    }

    public DungeonEntryInfo toProto(DungeonData data) {
        DungeonEntryInfo.Builder proto = DungeonEntryInfo.newBuilder();
        Optional.ofNullable(GameData.getDungeonSerialDataMap().get(data.getSerialId()))
            .map(DungeonSerialData::getId).map(this.player.getDungeonEntryItem().getBossRecordMap()::get)
            .ifPresent(bossRecord -> proto.setWeeklyBossResinDiscountInfo(bossRecord.toProto())
                .setBossChestNum(bossRecord.getTakeNum())
                .setMaxBossChestNum(bossRecord.getMaxTakeNumLimit())
                .setNextRefreshTime(bossRecord.getNextRefreshTime()));

        return proto.setDungeonId(data.getId())
            .setIsPassed(this.player.getDungeonEntryItem().getPassedDungeons().contains(data.getId()))
            .build();
    }

    public void addPassedDungeons(DungeonData dungeonData){
        this.player.getDungeonEntryItem().addDungeon(dungeonData.getId());
        this.player.getDungeonEntryItem().updateWeeklyBossInfo(dungeonData.getSerialId());
    }

    public List<Integer> getPlotDungeonById(int sceneId, int pointId){
        return this.plotDungeonEntries.get((sceneId << 16) + pointId);
    }

    public List<DungeonData> getDungeonEntries(int sceneId, int pointId) {
        val basicDungeons = Optional.ofNullable(GameData.getScenePointEntryById(sceneId, pointId))
            .map(ScenePointEntry::getPointData).map(PointData::getDungeonIds).stream().parallel()
            .flatMapToInt(Arrays::stream).mapToObj(GameData.getDungeonDataMap()::get)
            .filter(Objects::nonNull);

        val plotDungeons = Optional.ofNullable(getPlotDungeonById(sceneId, pointId)).stream()
            .flatMap(List::stream).map(dungeonId -> GameData.getDungeonDataMap().get(dungeonId.intValue()))
            .filter(Objects::nonNull);

        Stream<DungeonData> rosterDungeons;
        DungeonEntryItem.WeeklyBossRecord bossRecord = this.player.getDungeonEntryItem().getBossRecordMap().values().stream()
            .filter(bossRecord1 -> bossRecord1.getRosterCycleRecord() != null).findFirst().orElse(null);
        // assuming there is at least one roster dungeon
        if (bossRecord != null) {
            rosterDungeons = Optional.ofNullable(GameData.getScenePointEntryById(sceneId, pointId))
                .map(ScenePointEntry::getPointData).map(PointData::getDungeonRosterList).stream().parallel()
                .flatMapToInt(Arrays::stream).mapToObj(GameData.getDungeonRosterDataMap()::get).filter(Objects::nonNull)
                .map(rosterData -> rosterData.getRosterPool().get(bossRecord.getRosterCycleRecord().getSelectedPool()))
                .filter(Objects::nonNull).map(DungeonRosterData.RosterPool::getDungeonList).flatMapToInt(Arrays::stream)
                .mapToObj(GameData.getDungeonDataMap()::get);
        } else {
            rosterDungeons = Optional.ofNullable(GameData.getScenePointEntryById(sceneId, pointId))
                .map(ScenePointEntry::getPointData).map(PointData::getDungeonRosterList)
                .map(Arrays::stream).map(rosterStream -> rosterStream.parallel()
                    .mapToObj(GameData.getDungeonRosterDataMap()::get).filter(Objects::nonNull)
                    .filter(DungeonRosterData::nowIsAfterOpenTime)
                    .map(rosterData -> {
                        val rosterPool = rosterData.getRosterPool();
                        val serialId = rosterPool.stream().parallel().map(DungeonRosterData.RosterPool::getDungeonList)
                            .flatMapToInt(Arrays::stream).mapToObj(GameData.getDungeonDataMap()::get)
                            .filter(Objects::nonNull).map(DungeonData::getSerialId)
                            .findFirst().orElse(0);

                        int selectedPool = Optional.ofNullable(this.player.getDungeonEntryItem().getBossRecordMap().get(serialId))
                            .map(bossRecord1 -> bossRecord1.getRosterRecord(rosterData.getId()))
                            .map(DungeonEntryItem.RosterCycleRecord::getSelectedPool).orElse(-1);
                        return selectedPool == -1 ? new int[]{} : rosterPool.get(selectedPool).getDungeonList();
                    })
                    .flatMapToInt(Arrays::stream)
                    .mapToObj(GameData.getDungeonDataMap()::get)
                    .filter(Objects::nonNull)).orElse(Stream.of());
        }

        return Stream.concat(Stream.concat(basicDungeons, plotDungeons), rosterDungeons).parallel()
            .filter(data -> this.player.getLevel() >= data.getLimitLevel())
            .filter(data -> data.getId() != 69) // TODO, this is causing problem, find out why
            .toList();
    }
}
