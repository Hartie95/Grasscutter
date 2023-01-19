package emu.grasscutter.game.quest;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.MainQuestData;
import emu.grasscutter.data.excels.QuestData;
import emu.grasscutter.data.excels.QuestGlobalVarData;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.player.BasePlayerManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.enums.*;
import emu.grasscutter.server.packet.send.*;
import emu.grasscutter.utils.Position;
import io.netty.util.concurrent.FastThreadLocalThread;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

public class QuestManager extends BasePlayerManager {

    @Getter private final Player player;
    @Getter private final Int2ObjectMap<GameMainQuest> mainQuests;
    public static final ExecutorService eventExecutor;
    static {
        eventExecutor = new ThreadPoolExecutor(4, 4,
            60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000),
            FastThreadLocalThread::new, new ThreadPoolExecutor.AbortPolicy());
    }
    /*
        On SetPlayerBornDataReq, the server sends FinishedParentQuestNotify, with this exact
        parentQuestList. Captured on Game version 2.7
        Note: quest 40063 is already set to finished, with childQuest 4006406's state set to 3
    */

    private static Set<Integer> newPlayerMainQuests = Set.of(303,318,348,349,350,351,416,500,
        501,502,503,504,505,506,507,508,509,20000,20507,20509,21004,21005,21010,21011,21016,21017,
        21020,21021,21025,40063,70121,70124,70511,71010,71012,71013,71015,71016,71017,71555);

    /*
        On SetPlayerBornDataReq, the server sends ServerCondMeetQuestListUpdateNotify, with this exact
        addQuestIdList. Captured on Game version 2.7
        Total of 161...
     */
    /*
    private static Set<Integer> newPlayerServerCondMeetQuestListUpdateNotify = Set.of(3100101, 7104405, 2201601,
        7100801, 1907002, 7293301, 7193801, 7293401, 7193901, 7091001, 7190501, 7090901, 7190401, 7090801, 7190301,
        7195301, 7294801, 7195201, 7293001, 7094001, 7193501, 7293501, 7194001, 7293701, 7194201, 7194301, 7293801,
        7194901, 7194101, 7195001, 7294501, 7294101, 7194601, 7294301, 7194801, 7091301, 7290301, 2102401, 7216801,
        7190201, 7090701, 7093801, 7193301, 7292801, 7227828, 7093901, 7193401, 7292901, 7093701, 7193201, 7292701,
        7082402, 7093601, 7292601, 7193101, 2102301, 7093501, 7292501, 7193001, 7093401, 7292401, 7192901, 7093301,
        7292301, 7192801, 7294201, 7194701, 2100301, 7093201, 7212402, 7292201, 7192701, 7280001, 7293901, 7194401,
        7093101, 7212302, 7292101, 7192601, 7093001, 7292001, 7192501, 7216001, 7195101, 7294601, 2100900, 7092901,
        7291901, 7192401, 7092801, 7291801, 7192301, 2101501, 7092701, 7291701, 7192201, 7106401, 2100716, 7091801,
        7290801, 7191301, 7293201, 7193701, 7094201, 7294001, 7194501, 2102290, 7227829, 7193601, 7094101, 7091401,
        7290401, 7190901, 7106605, 7291601, 7192101, 7092601, 7291501, 7192001, 7092501, 7291401, 7191901, 7092401,
        7291301, 7191801, 7092301, 7211402, 7291201, 7191701, 7092201, 7291101, 7191601, 7092101, 7291001, 7191501,
        7092001, 7290901, 7191401, 7091901, 7290701, 7191201, 7091701, 7290601, 7191101, 7091601, 7290501, 7191001,
        7091501, 7290201, 7190701, 7091201, 7190601, 7091101, 7190101, 7090601, 7090501, 7090401, 7010701, 7090301,
        7090201, 7010103, 7090101
        );

    */

    public static long getQuestKey(int mainQuestId) {
        QuestEncryptionKey questEncryptionKey = GameData.getMainQuestEncryptionMap().get(mainQuestId);
        return questEncryptionKey != null ? questEncryptionKey.getEncryptionKey() : 0L;
    }
    public QuestManager(Player player) {

        super(player);
        this.player = player;
        this.mainQuests = new Int2ObjectOpenHashMap<>();
    }

    public void onPlayerBorn() {
        // TODO scan the quest and start the quest with acceptCond fulfilled
        // The off send 3 request in that order: 1. FinishedParentQuestNotify, 2. QuestListNotify, 3. ServerCondMeetQuestListUpdateNotify

        List<GameMainQuest> newQuests = this.addMultMainQuests(newPlayerMainQuests);
        for (GameMainQuest mainQuest : newQuests) {
            startMainQuest(mainQuest.getParentQuestId());
        }

        //getPlayer().sendPacket(new PacketFinishedParentQuestUpdateNotify(newQuests));
        //getPlayer().sendPacket(new PacketQuestListNotify(subQuests));
        //getPlayer().sendPacket(new PacketServerCondMeetQuestListUpdateNotify(newPlayerServerCondMeetQuestListUpdateNotify));
    }

    public void onLogin() {

        List<GameMainQuest> activeQuests = getActiveMainQuests();
        List<GameQuest> activeSubs = new ArrayList<>(activeQuests.size());
        for (GameMainQuest quest : activeQuests) {
            List<Position> rewindPos = quest.rewind(); // <pos, rotation>
            var activeQuest = quest.getActiveQuests();
            if (rewindPos != null) {
                getPlayer().getPosition().set(rewindPos.get(0));
                getPlayer().getRotation().set(rewindPos.get(1));
            }
            if(activeQuest!=null && rewindPos!=null){
                //activeSubs.add(activeQuest);
                //player.sendPacket(new PacketQuestProgressUpdateNotify(activeQuest));
            }
            quest.checkProgress();
        }
    }

    private List<GameMainQuest> addMultMainQuests(Set<Integer> mainQuestIds) {
        List<GameMainQuest> newQuests = new ArrayList<>();
        for (Integer id : mainQuestIds) {
            getMainQuests().put(id.intValue(),new GameMainQuest(this.player, id));
            getMainQuestById(id).save();
            newQuests.add(getMainQuestById(id));
        }
        return newQuests;
    }

    public void enableQuests() {
        onPlayerBorn();
    }

    /*
        Looking through mainQuests 72201-72208 and 72174, we can infer that a questGlobalVar's default value is 0
    */
    public int getQuestGlobalVarValue(int variable) {
        QuestGlobalVarData questGlobalVarData = GameData.getQuestGlobalVarDataMap().get(variable);
        return getPlayer().getQuestGlobalVariables().getOrDefault(
            variable, 
            questGlobalVarData == null ? 0 : questGlobalVarData.getDefaultValue());
    }

    public void setQuestGlobalVarValue(int variable, int setVal) {
        int prevVal = getQuestGlobalVarValue(variable);
        getPlayer().getQuestGlobalVariables().put(variable, setVal);
        int newVal = getQuestGlobalVarValue(variable);
        Grasscutter.getLogger().debug("Changed questGlobalVar {} value from {} to {}", variable, prevVal, newVal);
        triggerQuestGlobalVarAction(variable, newVal);
    }
    public void incQuestGlobalVarValue(int variable, int inc) {
        int prevVal = getQuestGlobalVarValue(variable);
        getPlayer().getQuestGlobalVariables().put(variable, prevVal + inc);
        int newVal = getQuestGlobalVarValue(variable);
        Grasscutter.getLogger().debug("Incremented questGlobalVar {} value from {} to {}", variable, prevVal, newVal);
        triggerQuestGlobalVarAction(variable, newVal);
    }

    public void decQuestGlobalVarValue(int variable, int dec) {
        int prevVal = getQuestGlobalVarValue(variable);
        getPlayer().getQuestGlobalVariables().put(variable, prevVal - dec);
        int newVal = getQuestGlobalVarValue(variable);
        Grasscutter.getLogger().debug("Decremented questGlobalVar {} value from {} to {}", variable, prevVal, newVal);
        triggerQuestGlobalVarAction(variable, newVal);
    }

    public void triggerQuestGlobalVarAction(int variable, int value) {
        queueEvent(QuestCond.QUEST_COND_QUEST_GLOBAL_VAR_EQUAL, variable, value);
        queueEvent(QuestCond.QUEST_COND_QUEST_GLOBAL_VAR_GREATER, variable, value);
        queueEvent(QuestCond.QUEST_COND_QUEST_GLOBAL_VAR_LESS, variable, value);
        getPlayer().sendPacket(new PacketQuestGlobalVarNotify(getPlayer()));
    }

    public GameMainQuest getMainQuestById(int mainQuestId) {
        return getMainQuests().get(mainQuestId);
    }

    public GameMainQuest getMainQuestByTalkId(int talkId) {
        int mainQuestId = GameData.getQuestTalkMap().getOrDefault(talkId, talkId / 100);
        return getMainQuestById(mainQuestId);
    }

    public GameQuest getQuestById(int questId) {
        QuestData questConfig = GameData.getQuestDataMap().get(questId);
        if (questConfig == null) {
            return null;
        }

        GameMainQuest mainQuest = getMainQuests().get(questConfig.getMainId());

        if (mainQuest == null) {
            return null;
        }

        return mainQuest.getChildQuests().get(questId);
    }

    public void forEachQuest(Consumer<GameQuest> callback) {
        for (GameMainQuest mainQuest : getMainQuests().values()) {
            for (GameQuest quest : mainQuest.getChildQuests().values()) {
                callback.accept(quest);
            }
        }
    }

    public void forEachMainQuest(Consumer<GameMainQuest> callback) {
        for (GameMainQuest mainQuest : getMainQuests().values()) {
            callback.accept(mainQuest);
        }
    }

    // TODO
    public void forEachActiveQuest(Consumer<GameQuest> callback) {
        for (GameMainQuest mainQuest : getMainQuests().values()) {
            for (GameQuest quest : mainQuest.getChildQuests().values()) {
                if (quest.getState() != QuestState.QUEST_STATE_FINISHED) {
                    callback.accept(quest);
                }
            }
        }
    }

    public GameMainQuest addMainQuest(QuestData questConfig) {
        GameMainQuest mainQuest = new GameMainQuest(getPlayer(), questConfig.getMainId());
        getMainQuests().put(mainQuest.getParentQuestId(), mainQuest);

        getPlayer().sendPacket(new PacketFinishedParentQuestUpdateNotify(mainQuest));

        return mainQuest;
    }

    public GameQuest addQuest(int questId) {
        QuestData questConfig = GameData.getQuestDataMap().get(questId);

        if (questConfig == null) {
            return null;
        }

        // Main quest
        GameMainQuest mainQuest = this.getMainQuestById(questConfig.getMainId());

        // Create main quest if it doesnt exist
        if (mainQuest == null) {
            mainQuest = addMainQuest(questConfig);
        }

        // Sub quest
        GameQuest quest = mainQuest.getChildQuestById(questId);

        // Forcefully start
        quest.start();
        checkQuestAlreadyFullfilled(quest);

        return quest;
    }

    public void startMainQuest(int mainQuestId) {
        var mainQuestData = GameData.getMainQuestDataMap().get(mainQuestId);

        if (mainQuestData == null) {
            return;
        }

        Arrays.stream(mainQuestData.getSubQuests())
            .min(Comparator.comparingInt(MainQuestData.SubQuestData::getOrder))
            .map(MainQuestData.SubQuestData::getSubId)
            .ifPresent(this::addQuest);
        //TODO find a better way then hardcoding to detect needed required quests
        if(mainQuestId == 355){
            startMainQuest(361);
            startMainQuest(418);
            startMainQuest(423);
            startMainQuest(20509);

        }
    }
    public void queueEvent(QuestCond condType, int... params) {
        queueEvent(condType, "", params);
    }
    public void queueEvent(QuestContent condType, int... params) {
        queueEvent(condType, "", params);
    }

    public void queueEvent(QuestContent condType, String paramStr, int... params) {
        eventExecutor.submit(() -> triggerEvent(condType, paramStr, params));
    }
    public void queueEvent(QuestCond condType, String paramStr, int... params) {
        eventExecutor.submit(() -> triggerEvent(condType, paramStr, params));
    }

    //QUEST_EXEC are handled directly by each subQuest

    public void triggerEvent(QuestCond condType, String paramStr, int... params) {
        Grasscutter.getLogger().debug("Trigger Event {}, {}, {}", condType, paramStr, params);
        List<GameMainQuest> checkMainQuests = this.getMainQuests().values().stream()
            .filter(i -> i.getState() != ParentQuestState.PARENT_QUEST_STATE_FINISHED)
            .toList();
        switch (condType) {
            //accept Conds
            case QUEST_COND_STATE_EQUAL:
            case QUEST_COND_STATE_NOT_EQUAL:
            case QUEST_COND_COMPLETE_TALK:
            case QUEST_COND_LUA_NOTIFY:
            case QUEST_COND_QUEST_VAR_EQUAL:
            case QUEST_COND_QUEST_VAR_GREATER:
            case QUEST_COND_QUEST_VAR_LESS:
            case QUEST_COND_PLAYER_LEVEL_EQUAL_GREATER:
            case QUEST_COND_QUEST_GLOBAL_VAR_EQUAL:
            case QUEST_COND_QUEST_GLOBAL_VAR_GREATER:
            case QUEST_COND_QUEST_GLOBAL_VAR_LESS:
            case QUEST_COND_PACK_HAVE_ITEM:
            case QUEST_COND_ITEM_NUM_LESS_THAN:
            case QUEST_COND_ACTIVITY_OPEN:
            case QUEST_COND_ACTIVITY_END:
            case QUEST_COND_ACTIVITY_COND:
                for (GameMainQuest mainquest : checkMainQuests) {
                    mainquest.tryAcceptSubQuests(condType, paramStr, params);
                }
                break;

            // unused
            case QUEST_COND_PLAYER_CHOOSE_MALE:
            default:
                Grasscutter.getLogger().error("Unhandled QuestCondition {}", condType);
        }
    }
    public void triggerEvent(QuestContent condType, String paramStr, int... params) {
        Grasscutter.getLogger().debug("Trigger Event {}, {}, {}", condType, paramStr, params);
        List<GameMainQuest> checkMainQuests = this.getMainQuests().values().stream()
            .filter(i -> i.getState() != ParentQuestState.PARENT_QUEST_STATE_FINISHED)
            .toList();
        switch (condType) {
            //fail Conds
            case QUEST_CONTENT_NOT_FINISH_PLOT:
            case QUEST_CONTENT_ANY_MANUAL_TRANSPORT:
                for (GameMainQuest mainquest : checkMainQuests) {
                    mainquest.tryFailSubQuests(condType, paramStr, params);
                }
                break;
            //finish Conds
            case QUEST_CONTENT_COMPLETE_TALK:
            case QUEST_CONTENT_FINISH_PLOT:
            case QUEST_CONTENT_COMPLETE_ANY_TALK:
            case QUEST_CONTENT_QUEST_VAR_EQUAL:
            case QUEST_CONTENT_QUEST_VAR_GREATER:
            case QUEST_CONTENT_QUEST_VAR_LESS:
            case QUEST_CONTENT_ENTER_DUNGEON:
            case QUEST_CONTENT_ENTER_MY_WORLD_SCENE:
            case QUEST_CONTENT_INTERACT_GADGET:
            case QUEST_CONTENT_TRIGGER_FIRE:
            case QUEST_CONTENT_UNLOCK_TRANS_POINT:
            case QUEST_CONTENT_UNLOCK_AREA:
            case QUEST_CONTENT_SKILL:
            case QUEST_CONTENT_OBTAIN_ITEM:
            case QUEST_CONTENT_MONSTER_DIE:
            case QUEST_CONTENT_DESTROY_GADGET:
            case QUEST_CONTENT_PLAYER_LEVEL_UP:
            case QUEST_CONTENT_USE_ITEM:
            case QUEST_CONTENT_ENTER_VEHICLE:
            case QUEST_CONTENT_FINISH_DUNGEON:
                for (GameMainQuest mainQuest : checkMainQuests) {
                    mainQuest.tryFinishSubQuests(condType, paramStr, params);
                }
                break;

            //finish Or Fail Conds
            case QUEST_CONTENT_GAME_TIME_TICK:
            case QUEST_CONTENT_QUEST_STATE_EQUAL:
            case QUEST_CONTENT_ADD_QUEST_PROGRESS:
            case QUEST_CONTENT_LEAVE_SCENE:
            case QUEST_CONTENT_ITEM_LESS_THAN:
            case QUEST_CONTENT_KILL_MONSTER:
            case QUEST_CONTENT_LUA_NOTIFY:
            case QUEST_CONTENT_ENTER_MY_WORLD:
            case QUEST_CONTENT_ENTER_ROOM:
            case QUEST_CONTENT_FAIL_DUNGEON:
                for (GameMainQuest mainQuest : checkMainQuests) {
                    mainQuest.tryFailSubQuests(condType, paramStr, params);
                    mainQuest.tryFinishSubQuests(condType, paramStr, params);
                }
                break;

            //Unused
            case QUEST_CONTENT_QUEST_STATE_NOT_EQUAL:
            case QUEST_CONTENT_WORKTOP_SELECT:
            default:
                Grasscutter.getLogger().error("Unhandled QuestTrigger {}", condType);
        }
    }

    /**
     * TODO maybe trigger them delayed to allow basic communication finish first
     * @param quest
     */
    public void checkQuestAlreadyFullfilled(GameQuest quest){
        Grasscutter.getGameServer().getScheduler().scheduleDelayedTask(() -> {
            for(var condition : quest.getQuestData().getFinishCond()){
                switch (condition.getType()) {
                    case QUEST_CONTENT_OBTAIN_ITEM, QUEST_CONTENT_ITEM_LESS_THAN -> {
                        //check if we already own enough of the item
                        var item = getPlayer().getInventory().getItemByGuid(condition.getParam()[0]);
                        queueEvent(condition.getType(), condition.getParam()[0], item != null ? item.getCount() : 0);
                    }
                    case QUEST_CONTENT_UNLOCK_TRANS_POINT -> {
                        var scenePoints = getPlayer().getUnlockedScenePoints().get(condition.getParam()[0]);
                        if (scenePoints != null && scenePoints.contains(condition.getParam()[1])) {
                            queueEvent(condition.getType(), condition.getParam()[0], condition.getParam()[1]);
                        }
                    }
                    case QUEST_CONTENT_UNLOCK_AREA -> {
                        var sceneAreas = getPlayer().getUnlockedSceneAreas().get(condition.getParam()[0]);
                        if (sceneAreas != null && sceneAreas.contains(condition.getParam()[1])) {
                            queueEvent(condition.getType(), condition.getParam()[0], condition.getParam()[1]);
                        }
                    }
                }
            }
        }, 1);
    }

    public List<QuestGroupSuite> getSceneGroupSuite(int sceneId) {
        return getMainQuests().values().stream()
            .filter(i -> i.getState() != ParentQuestState.PARENT_QUEST_STATE_FINISHED)
            .map(GameMainQuest::getQuestGroupSuites)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(i -> i.getScene() == sceneId)
            .toList();
    }
    public void loadFromDatabase() {
        List<GameMainQuest> quests = DatabaseHelper.getAllQuests(getPlayer());

        for (GameMainQuest mainQuest : quests) {
            boolean cancelAdd = false;
            mainQuest.setOwner(this.getPlayer());

            for (GameQuest quest : mainQuest.getChildQuests().values()) {
                QuestData questConfig = GameData.getQuestDataMap().get(quest.getSubQuestId());

                if (questConfig == null) {
                    mainQuest.delete();
                    cancelAdd = true;
                    break;
                }

                quest.setMainQuest(mainQuest);
                quest.setConfig(questConfig);
            }

            if (!cancelAdd) {
                this.getMainQuests().put(mainQuest.getParentQuestId(), mainQuest);
            }
        }
    }

    public List<GameMainQuest> getActiveMainQuests() {
        return getMainQuests().values().stream().filter(p -> !p.isFinished()).toList();
    }
}
