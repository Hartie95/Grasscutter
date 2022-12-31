package emu.grasscutter.data.excels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.quest.enums.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ResourceType(name = "QuestExcelConfigData.json")
@Getter
@ToString
public class QuestData extends GameResource {
    private int subId;
    private int mainId;
    private int order;
    private long descTextMapHash;

    private boolean finishParent;
    private boolean isRewind;

    private LogicType acceptCondComb;
    private LogicType finishCondComb;
    private LogicType failCondComb;

    private List<QuestAcceptCondition> acceptCond;
    private List<QuestContentCondition> finishCond;
    private List<QuestContentCondition> failCond;
    private List<QuestExecParam> beginExec;
    private List<QuestExecParam> finishExec;
    private List<QuestExecParam> failExec;
    private Guide guide;
    private List<Integer> trialAvatarList;

    //ResourceLoader not happy if you remove getId() ~~
    public int getId() {
        return subId;
    }
    //Added getSubId() for clarity
    public int getSubId() {return subId;}

    public int getMainId() {
        return mainId;
    }

    public int getOrder() {
        return order;
    }

    public long getDescTextMapHash() {
        return descTextMapHash;
    }

    public boolean finishParent() {
        return finishParent;
    }

    public boolean isRewind() {
        return isRewind;
    }

    public LogicType getAcceptCondComb() {
        return acceptCondComb == null ? LogicType.LOGIC_NONE : acceptCondComb;
    }

    public List<QuestAcceptCondition> getAcceptCond() {
        return acceptCond;
    }

    public LogicType getFinishCondComb() {
        return finishCondComb == null ? LogicType.LOGIC_NONE : finishCondComb;
    }

    public List<QuestContentCondition> getFinishCond() {
        return finishCond;
    }

    public LogicType getFailCondComb() {
        return failCondComb == null ? LogicType.LOGIC_NONE : failCondComb;
    }

    public List<QuestContentCondition> getFailCond() {
        return failCond;
    }

    public void onLoad() {
        this.acceptCond = acceptCond.stream().filter(p -> p.getType() != null).toList();
        this.finishCond = finishCond.stream().filter(p -> p.getType() != null).toList();
        this.failCond = failCond.stream().filter(p -> p.getType() != null).toList();

        this.beginExec = beginExec.stream().filter(p -> p.type != null).toList();
        this.finishExec = finishExec.stream().filter(p -> p.type != null).toList();
        this.failExec = failExec.stream().filter(p -> p.type != null).toList();

        addToCache();
    }

    private void addToCache(){
        getFinishCond().stream().forEach(x -> {
            if (x.getType() != QuestContent.QUEST_CONTENT_ENTER_DUNGEON) return;
            GameData.getQuestDungeonMap().computeIfAbsent(x.getParam()[1], s -> new HashMap<>())
                .put(getSubId(), x.getParam()[0]);
        });
        
        if (getAcceptCond() == null) {
            Grasscutter.getLogger().warn("missing AcceptConditions for quest {}", getSubId());
            return;
        }
        val cacheMap = GameData.getBeginCondQuestMap();
        if (getAcceptCond().isEmpty()) {
            val list = cacheMap.computeIfAbsent(QuestData.questConditionKey(QuestCond.QUEST_COND_NONE, 0, null), e -> new ArrayList<>());
            list.add(this);
        } else {
            getAcceptCond().forEach(questCondition -> {
                if (questCondition.getType() == null) {
                    Grasscutter.getLogger().warn("null accept type for quest {}", getSubId());
                    return;
                }
                val key = questCondition.asKey();
                val list = cacheMap.computeIfAbsent(key, e -> new ArrayList<>());
                list.add(this);
            });
        }
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuestExecParam {
        @SerializedName("_type")
        QuestExec type;
        @SerializedName("_param")
        String[] param;
        @SerializedName("_count")
        String count;
    }

    public static class QuestAcceptCondition extends QuestCondition<QuestCond>{ }
    public static class QuestContentCondition extends QuestCondition<QuestContent>{ }

    @Data
    public static class QuestCondition<TYPE extends Enum<?> & QuestTrigger> {
        @SerializedName("_type")
        private TYPE type;
        @SerializedName("_param")
        private int[] param;
        @SerializedName("_param_str")
        private String paramStr;
        @SerializedName("_count")
        private int count;

        public String asKey(){
            return questConditionKey(getType(),getParam()[0],getParamStr());
        }
    }

    @Data
    public static class Guide {
        private String type;
        private List<String> param;
        private int guideScene;
    }

    public static String questConditionKey(@Nonnull Enum<?> type, int firstParam, @Nullable String paramsStr){
        return type.name() + firstParam + (paramsStr != null ? paramsStr:"");
    }
}
