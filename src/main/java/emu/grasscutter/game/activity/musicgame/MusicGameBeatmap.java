package emu.grasscutter.game.activity.musicgame;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import emu.grasscutter.database.DatabaseHelper;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import emu.grasscutter.net.proto.UgcMusicBriefInfoOuterClass.UgcMusicBriefInfo;
import emu.grasscutter.net.proto.UgcMusicNoteOuterClass.UgcMusicNote;
import emu.grasscutter.net.proto.UgcMusicRecordOuterClass.UgcMusicRecord;
import emu.grasscutter.net.proto.UgcMusicTrackOuterClass.UgcMusicTrack;

import java.util.List;
import java.util.Random;

@Entity("music_game_beatmaps")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class MusicGameBeatmap {

    @Id
    long musicShareId;
    int authorUid;
    int musicId;
    int musicNoteCount;
    int savePosition;
    int savePageType;
    int version;
    List<Integer> afterNoteList;
    List<Integer> beforeNoteList;
    int timeLineEditTime;
    int realTimeEditTime;
    int publishTime;
    int maxScore;
    int createTime;

    List<List<BeatmapNote>> beatmap;

    public static MusicGameBeatmap getByShareId(long musicShareId){
        return DatabaseHelper.getMusicGameBeatmap(musicShareId);
    }

    public void save(){
        if(musicShareId == 0){
            musicShareId = new Random().nextLong(100000000000000L,999999999999999L);
        }
        DatabaseHelper.saveMusicGameBeatmap(this);
    }

    public static List<List<BeatmapNote>> parse(List<UgcMusicTrack> beatmapItemListList) {
        return beatmapItemListList.stream()
            .map(item -> item.getMusicNoteListList().stream()
                .map(BeatmapNote::parse)
                .toList())
            .toList();
    }

    public UgcMusicRecord toProto(){
        return UgcMusicRecord.newBuilder()
            .setMusicId(musicId)
            .addAllMusicTrackList(beatmap.stream()
                .map(this::musicBeatmapListToProto)
                .toList())
            .build();
    }

    public UgcMusicBriefInfo.Builder toBriefProto(){
        var player = DatabaseHelper.getPlayerByUid(authorUid);

        return UgcMusicBriefInfo.newBuilder()
            .setMusicId(musicId)
            .setNoteCount(musicNoteCount)
            .setUgcGuid(musicShareId)
            .setMaxScore(maxScore)
            .setPublishTime(createTime)
            .setCreatorNickname(player.getNickname())
            .setSavePageType(savePageType)
            .setVersion(version)
            .addAllAfterNoteList(afterNoteList)
            .addAllBeforeNoteList(beforeNoteList)
            .setTimeLineEditTime(timeLineEditTime)
            .setPublishTime(publishTime)
            .setRealTimeEditTime(realTimeEditTime)
            ;
    }

    private UgcMusicTrack musicBeatmapListToProto(List<BeatmapNote> beatmapNoteList){
        return UgcMusicTrack.newBuilder()
            .addAllMusicNoteList(beatmapNoteList.stream()
                .map(BeatmapNote::toProto)
                .toList())
            .build();
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder(builderMethodName = "of")
    @Entity
    public static class BeatmapNote{
        int startTime;
        int endTime;

        public static BeatmapNote parse(UgcMusicNote note){
            return BeatmapNote.of()
                .startTime(note.getStartTime())
                .endTime(note.getEndTime())
                .build();
        }

        public UgcMusicNote toProto(){
            return UgcMusicNote.newBuilder()
                .setStartTime(startTime)
                .setEndTime(endTime)
                .build();
        }
    }
}
