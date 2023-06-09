package emu.grasscutter.data.common.quest;

import dev.morphia.annotations.Entity;
import emu.grasscutter.game.quest.enums.QuestShowType;
import emu.grasscutter.game.quest.enums.QuestType;
import lombok.Data;
import java.util.List;
import java.util.Objects;

@Data
public class MainQuestData {
    private int id;
    private int collectionId;
    private int series;
    private int chapterId;
    private QuestType taskType;
    private String luaPath;
    private int recommendedLevel;
    private QuestShowType showType;
    private boolean repeatable;
    private int activityId;
    private int activityType;
    private String mainQuestTag;
    private String activeMode;
    private boolean showRedPoint;
    private int taskId;
    private long videoKey;



    private boolean suggestTrackOutOfOrder;
    private int[] suggestTrackMainQuestList;
    private int[] rewardIdList;

    private SubQuestData[] subQuests;
    private List<TalkData> talks;
    private long[] preloadLuaList;

    public int getId() {
        return id;
    }

    private long titleTextMapHash;

    public int getSeries() {
        return series;
    }

    public QuestType getType() {
        return taskType;
    }

    public long getTitleTextMapHash() {
        return titleTextMapHash;
    }

    public int[] getSuggestTrackMainQuestList() {
        return suggestTrackMainQuestList;
    }

    public int[] getRewardIdList() {
        return rewardIdList;
    }

    public SubQuestData[] getSubQuests() {
        return subQuests;
    }
    public List<TalkData> getTalks() {
        return talks;
    }

    public void onLoad() {
        this.talks = talks.stream().filter(Objects::nonNull).toList();
    }


    @Data @Entity
    public static class TalkData {
        private int id;
        private String heroTalk;

        public TalkData() {}
        public TalkData(int id, String heroTalk) {
            this.id = id;
            this.heroTalk = heroTalk;
        }
    }
}
