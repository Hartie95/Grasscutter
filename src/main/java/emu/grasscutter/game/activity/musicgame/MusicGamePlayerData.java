package emu.grasscutter.game.activity.musicgame;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.MusicGameBasicData;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.net.proto.MusicGameRecordOuterClass;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import emu.grasscutter.net.proto.UgcMusicBriefInfoOuterClass.UgcMusicBriefInfo;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class MusicGamePlayerData {
    Map<Integer, MusicGameRecord> musicGameRecord;
    Map<Long, CustomBeatmapRecord> personalCustomBeatmapRecord;
    Map<Long, CustomBeatmapRecord> othersCustomBeatmapRecord;

    public static MusicGamePlayerData create() {
        return MusicGamePlayerData.of()
            .musicGameRecord(GameData.getMusicGameBasicDataMap().values().stream()
                .collect(Collectors.toMap(MusicGameBasicData::getId, MusicGamePlayerData.MusicGameRecord::create)))
            .personalCustomBeatmapRecord(new HashMap<>())
            .othersCustomBeatmapRecord(new HashMap<>())
            .build();
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder(builderMethodName = "of")
    public static class MusicGameRecord {
        int musicId;
        int maxCombo;
        int maxScore;

        public static MusicGameRecord create(MusicGameBasicData musicGameBasicData) {
            return MusicGameRecord.of()
                .musicId(musicGameBasicData.getId())
                .build();
        }

        public MusicGameRecordOuterClass.MusicGameRecord toProto() {
            return MusicGameRecordOuterClass.MusicGameRecord.newBuilder()
                .setIsUnlock(true)
                .setMaxCombo(maxCombo)
                .setMaxScore(maxScore)
                .build();
        }
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder(builderMethodName = "of")
    public static class CustomBeatmapRecord {
        long musicShareId;
        int score;
        boolean settle;

        public UgcMusicBriefInfo.Builder toPersonalBriefProto() {
            var musicGameBeatmap = MusicGameBeatmap.getByShareId(musicShareId);
            if(musicGameBeatmap == null){
                return null;
            }

            val player = DatabaseHelper.getPlayerByUid(musicGameBeatmap.getAuthorUid());
            val nickname = player!=null ? player.getNickname() : "UNKNOWN";
            return UgcMusicBriefInfo.newBuilder()
                .setIsPublished(true)
                .setSaveTime(musicGameBeatmap.getCreateTime())
                .setMusicId(musicGameBeatmap.getMusicId())
                .setMaxScore(musicGameBeatmap.getMaxScore())
                .setSaveIdx(musicGameBeatmap.getSavePosition())
                .setSavePageType(musicGameBeatmap.getSavePageType())
                .setVersion(musicGameBeatmap.getVersion())
                .addAllAfterNoteList(musicGameBeatmap.getAfterNoteList())
                .addAllBeforeNoteList(musicGameBeatmap.getBeforeNoteList())
                .setTimeLineEditTime(musicGameBeatmap.getTimeLineEditTime())
                .setPublishTime(musicGameBeatmap.getPublishTime())
                .setRealTimeEditTime(musicGameBeatmap.getRealTimeEditTime())
                .setNoteCount(musicGameBeatmap.getMusicNoteCount())
                .setUgcGuid(musicGameBeatmap.getMusicShareId())
                .setCreatorNickname(nickname);
        }

        public UgcMusicBriefInfo.Builder toOthersBriefProto() {
            var musicGameBeatmap = MusicGameBeatmap.getByShareId(musicShareId);

            return musicGameBeatmap.toBriefProto()
                .setSelfMaxScore(score)
                .setIsPlayed(settle);
        }

    }
}


