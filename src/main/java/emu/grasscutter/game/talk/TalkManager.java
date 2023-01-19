package emu.grasscutter.game.talk;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.TalkConfigData;
import emu.grasscutter.data.binout.MainQuestData.TalkData;
import emu.grasscutter.game.player.BasePlayerManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameMainQuest;

import lombok.Getter;

import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_COMPLETE_ANY_TALK;
import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_COMPLETE_TALK;
import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_COMPLETE_TALK;

public class TalkManager extends BasePlayerManager {
    @Getter private final Player player;

    public TalkManager(Player player) {
        super(player);
        this.player = player;
    }

    public void triggerTalkAction(int talkId) {
        TalkConfigData talkData = GameData.getTalkConfigDataMap().get(talkId);
        if (talkData == null || talkData.getFinishExec().isEmpty()) return;

        talkData.getFinishExec().forEach(e -> getPlayer().getServer().getTalkSystem().triggerExec(getPlayer(), talkData, e));
        getPlayer().getQuestManager().queueEvent(QUEST_CONTENT_COMPLETE_ANY_TALK, talkId);
        getPlayer().getQuestManager().queueEvent(QUEST_CONTENT_COMPLETE_TALK, talkId);
        getPlayer().getQuestManager().queueEvent(QUEST_COND_COMPLETE_TALK, talkId);
        saveTalkToQuest(talkId, talkData.getQuestId());
    }

    public void saveTalkToQuest(int talkId, int mainQuestId) {
        // TODO, problem with this is that some talks for activity also have 
        // quest id, which isnt present in QuestExcels
        GameMainQuest mainQuest = getPlayer().getQuestManager().getMainQuestById(mainQuestId);
        if (mainQuest == null) return;

        mainQuest.getTalks().put(talkId, new TalkData(talkId, ""));
    }
}