package emu.grasscutter.data.common.quest;

import emu.grasscutter.game.quest.enums.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.val;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Data
public class SubQuestData {
    private int subId;
    private int mainId;
    private int order;


    private boolean isMpBlock;
    private boolean finishParent;
    private boolean isRewind;

    private QuestShowType showType;
    private String luaPath;
    private String versionBegin;
    private String versionEnd;

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
    private List<Integer> exclusiveNpcList;
    private int exclusiveNpcPriority;
    private List<Integer> sharedNpcList;
    private List<Integer> exclusivePlaceList;

    private List<GainItem> gainItems;

    private long descTextMapHash;
    private long stepDescTextMapHash;

    public static String questConditionKey(@Nonnull Enum<?> type, int firstParam, @Nullable String paramsStr) {
        return type.name() + firstParam + (paramsStr != null ? paramsStr : "");
    }

    @Data
    public static class GainItem {
        private int itemId;
        private int count;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuestExecParam {
        QuestExec type;
        String[] param;
        String count;
    }

    public static class QuestAcceptCondition extends QuestCondition<QuestCond> { }

    public static class QuestContentCondition extends QuestCondition<QuestContent> { }

    @Data
    public static class QuestCondition<TYPE extends Enum<?> & QuestTrigger> {
        private TYPE type;
        private int[] param;
        @Nullable
        private String paramString;
        private int count;

        public String asKey() {
            val param = getParam();
            return questConditionKey(getType(), param!=null && param.length>0 ? getParam()[0] : 0, getParamString());
        }
    }

    @Data
    public static class Guide {
        private String type;
        private List<String> param;
        private int guideScene;
    }
}
