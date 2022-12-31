package emu.grasscutter.game.quest;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Transient;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.ChapterData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.data.excels.TriggerExcelConfigData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.quest.enums.LogicType;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.net.proto.ChapterStateOuterClass;
import emu.grasscutter.net.proto.QuestOuterClass.Quest;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.server.packet.send.PacketChapterStateNotify;
import emu.grasscutter.server.packet.send.PacketDelQuestNotify;
import emu.grasscutter.server.packet.send.PacketQuestListUpdateNotify;
import emu.grasscutter.server.packet.send.PacketQuestProgressUpdateNotify;
import emu.grasscutter.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;

import static emu.grasscutter.game.dungeons.enums.DungeonPassConditionType.DUNGEON_COND_FINISH_QUEST;
import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_STATE_EQUAL;
import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_FINISH_PLOT;
import static emu.grasscutter.game.quest.enums.QuestContent.QUEST_CONTENT_QUEST_STATE_EQUAL;

@Entity
public class GameQuest {
    @Transient @Getter @Setter private GameMainQuest mainQuest;
    @Transient @Getter private QuestData questData;

    @Getter private int subQuestId;
    @Getter private int mainQuestId;
    @Getter @Setter
    public QuestState state;

    @Getter @Setter private int startTime;
    @Getter @Setter private int acceptTime;
    @Getter @Setter private int finishTime;

    @Getter private int[] finishProgressList;
    @Getter private int[] failProgressList;
    @Transient @Getter private Map<String, TriggerExcelConfigData> triggerData;
    @Getter private Map<String, Boolean> triggers;
    private transient Bindings bindings;

    @Deprecated // Morphia only. Do not use.
    public GameQuest() {}

    public GameQuest(GameMainQuest mainQuest, QuestData questData) {
        this.mainQuest = mainQuest;
        this.subQuestId = questData.getId();
        this.mainQuestId = questData.getMainId();
        this.questData = questData;
        this.state = QuestState.QUEST_STATE_UNSTARTED;
        this.triggerData = new HashMap<>();
        this.triggers = new HashMap<>();
    }

    public void start(boolean shouldNotify) {
        clearProgress(false);
        this.acceptTime = Utils.getCurrentSeconds();
        this.startTime = this.acceptTime;
        this.state = QuestState.QUEST_STATE_UNFINISHED;

        checkAndLoadTrigger();

        if (shouldNotify) 
            getOwner().sendPacket(new PacketQuestListUpdateNotify(this));

        getOwner().getQuestManager().checkChapter(subQuestId, true);

        //Some subQuests and talks become active when some other subQuests are unfinished (even from different MainQuests)
        getOwner().getQuestManager().queueEvent(QUEST_CONTENT_QUEST_STATE_EQUAL, getSubQuestId(), getState().getValue());
        getOwner().getQuestManager().queueEvent(QUEST_COND_STATE_EQUAL, getSubQuestId(), getState().getValue());

        getQuestData().getBeginExec().forEach(e -> getOwner().getServer().getQuestSystem().triggerExec(this, e, e.getParam()));
        getOwner().getQuestManager().checkQuestAlreadyFullfilled(this);

        Grasscutter.getLogger().debug("Quest {} is started", subQuestId);
    }

    public void checkAndLoadTrigger(){
        questData.getFinishCond().stream()
            .filter(p -> p.getType() == QuestContent.QUEST_CONTENT_TRIGGER_FIRE)
            .forEach(cond -> {
                TriggerExcelConfigData newTrigger = GameData.getTriggerExcelConfigDataMap().get(cond.getParam()[0]);
                if(newTrigger == null){
                    return;
                }
                if (this.triggerData == null) {
                    this.triggerData = new HashMap<>();
                }
                triggerData.put(newTrigger.getTriggerName(), newTrigger);
                triggers.put(newTrigger.getTriggerName(), false);
                SceneGroup group = SceneGroup.of(newTrigger.getGroupId()).load(newTrigger.getSceneId());
                getOwner().getWorld().getSceneById(newTrigger.getSceneId()).loadTriggerFromGroup(group, newTrigger.getTriggerName());
            });
    }

    public String getTriggerNameById(int id) {
        TriggerExcelConfigData trigger = GameData.getTriggerExcelConfigDataMap().get(id);
        if (trigger != null) {
            return trigger.getTriggerName();
        }
        //return empty string if can't find trigger
        return "";
    }

    public Player getOwner() {
        return this.getMainQuest().getOwner();
    }

    public void setConfig(QuestData config) {
        if (config == null || getSubQuestId() != config.getId()) return;
        this.questData = config;
    }

    public void setFinishProgress(int index, int value) {
        finishProgressList[index] = value;
    }

    public void setFailProgress(int index, int value) {
        failProgressList[index] = value;
    }

    private boolean shouldFinish(){
        return LogicType.calculate(questData.getFinishCondComb(), finishProgressList);
    }

    private boolean shouldFail(){
        return LogicType.calculate(questData.getFailCondComb(), failProgressList);
    }

    public boolean isUnfinished() {
        return getState() == QuestState.QUEST_STATE_UNFINISHED;
    }

    public void tryFinish(QuestContent condType, String paramStr, int... params){
        val finishCond = questData.getFinishCond();

        for (int i = 0; i < finishCond.size(); i++) {
            val condition = finishCond.get(i);
            if (condition.getType() == condType) {
                boolean result = this.getOwner().getServer().getQuestSystem().triggerContent(this, condition, paramStr, params);
                setFinishProgress(i, result ? 1 : 0);
                if (result) {
                    getOwner().getSession().send(new PacketQuestProgressUpdateNotify(this));
                }
            }
        }

        if(shouldFinish()){
            finish();
        }
    }

    public void tryFail(QuestContent condType, String paramStr, int... params){
        val failCond = questData.getFailCond();

        for (int i = 0; i < questData.getFailCond().size(); i++) {
            val condition = failCond.get(i);
            if (condition.getType() == condType) {
                boolean result = this.getOwner().getServer().getQuestSystem().triggerContent(this, condition, paramStr, params);
                setFailProgress(i, result ? 1 : 0);
                if (result) {
                    getOwner().getSession().send(new PacketQuestProgressUpdateNotify(this));
                }
            }
        }

        if(shouldFail()){
            fail();
        }
    }


    public boolean clearProgress(boolean notifyDelete){
        //TODO improve
        var oldState = state;
        if (questData.getFinishCond() != null && questData.getFinishCond().size() != 0) {
            this.finishProgressList = new int[questData.getFinishCond().size()];
        }

        if (questData.getFailCond() != null && questData.getFailCond().size() != 0) {
            this.failProgressList = new int[questData.getFailCond().size()];
        }
        setState(QuestState.QUEST_STATE_UNSTARTED);
        finishTime = 0;
        acceptTime = 0;
        startTime = 0;
        if(oldState == QuestState.QUEST_STATE_UNSTARTED){
            return false;
        }
        if(notifyDelete) {
            getOwner().sendPacket(new PacketDelQuestNotify(getSubQuestId()));
        }
        save();
        return true;
    }

    public void finish() {
        this.state = QuestState.QUEST_STATE_FINISHED;
        this.finishTime = Utils.getCurrentSeconds();

        getOwner().sendPacket(new PacketQuestListUpdateNotify(this));

        if (getQuestData().finishParent()) {
            // This quest finishes the questline - the main quest will also save the quest to db, so we don't have to call save() here
            getMainQuest().finish();
        } else {
            this.save();
        }

        getQuestData().getFinishExec().forEach(e -> getOwner().getServer().getQuestSystem().triggerExec(this, e, e.getParam()));

        getOwner().getQuestManager().checkChapter(subQuestId, false);

        triggerFinishQuestEvents();
        triggerQuestWorkarounds();

        Grasscutter.getLogger().debug("Quest {} is finished", subQuestId);
    }

    private void triggerFinishQuestEvents() {
        //Some subQuests have conditions that subQuests are finished (even from different MainQuests)
        val questManager = getOwner().getQuestManager();
        getOwner().getQuestManager().queueEvent(QUEST_CONTENT_QUEST_STATE_EQUAL, getSubQuestId(), getState().getValue());
        getOwner().getQuestManager().queueEvent(QUEST_COND_STATE_EQUAL, getSubQuestId(), getState().getValue());
        // getOwner().getQuestManager().queueEvent(QUEST_CONTENT_FINISH_PLOT, getSubQuestId());
        getOwner().getScene().triggerDungeonEvent(DUNGEON_COND_FINISH_QUEST, getSubQuestId());
        getOwner().getProgressManager().tryUnlockOpenStates();
    }

    private void triggerQuestWorkarounds(){
        // hard coding to give quest item
        getOwner().getInventory().addItem(switch (getQuestData().getSubId()) {
            case 35402 -> 1021; // amber itemId
            case 46203 -> 100171; // kaeya story quest item
            default -> 0;
        }, 1, ActionReason.QuestItem);
    }

    //TODO
    public void fail() {
        this.state = QuestState.QUEST_STATE_FAILED;
        this.finishTime = Utils.getCurrentSeconds();
        this.save();

        getOwner().sendPacket(new PacketQuestListUpdateNotify(this));

        //Some subQuests have conditions that subQuests fail (even from different MainQuests)
        getOwner().getQuestManager().queueEvent(QUEST_CONTENT_QUEST_STATE_EQUAL, this.subQuestId, this.state.getValue());
        getOwner().getQuestManager().queueEvent(QUEST_COND_STATE_EQUAL, this.subQuestId, this.state.getValue());

        getQuestData().getFailExec().forEach(e -> getOwner().getServer().getQuestSystem().triggerExec(this, e, e.getParam()));

        Grasscutter.getLogger().debug("Quest {} is failed", subQuestId);

        if (getQuestData().getTrialAvatarList() == null) return;
        
        getQuestData().getTrialAvatarList().forEach(t -> getOwner().removeTrialAvatar(t));
    }

    // Return true if it did the rewind
    public boolean rewind(boolean notifyDelete) {
        getMainQuest().getChildQuests().values().stream().filter(p -> p.getQuestData().getOrder() > this.getQuestData().getOrder()).forEach(q -> {
            q.clearProgress(notifyDelete);
        });
        clearProgress(notifyDelete);
        this.start(notifyDelete);
        return true;
    }

    public void save() {
        getMainQuest().save();
    }

    public Quest toProto() {
        Quest.Builder proto = Quest.newBuilder()
                .setQuestId(getSubQuestId())
                .setState(getState().getValue())
                .setParentQuestId(getMainQuestId())
                .setStartTime(getStartTime())
                .setStartGameTime(438)
                .setAcceptTime(getAcceptTime());

        if (getFinishProgressList() != null) {
            for (int i : getFinishProgressList()) {
                proto.addFinishProgressList(i);
            }
        }

        if (getFailProgressList() != null) {
            for (int i : getFailProgressList()) {
                proto.addFailProgressList(i);
            }
        }

        return proto.build();
    }
}
