package emu.grasscutter.game.player;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScenePointEntry;
import emu.grasscutter.data.common.quest.SubQuestData;
import emu.grasscutter.data.excels.AvatarReplaceCostumeData;
import emu.grasscutter.data.excels.OpenStateData;
import emu.grasscutter.data.excels.OpenStateData.OpenStateCondType;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.quest.GameMainQuest;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.enums.ParentQuestState;
import emu.grasscutter.game.quest.enums.QuestCond;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.packet.send.PacketOpenStateChangeNotify;
import emu.grasscutter.server.packet.send.PacketOpenStateUpdateNotify;
import emu.grasscutter.server.packet.send.PacketSceneAreaUnlockNotify;
import emu.grasscutter.server.packet.send.PacketScenePointUnlockNotify;
import emu.grasscutter.server.packet.send.PacketSetOpenStateRsp;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import static emu.grasscutter.scripts.constants.EventType.EVENT_UNLOCK_TRANS_POINT;

@SuppressWarnings("SpellCheckingInspection")
public class PlayerProgressManager extends BasePlayerDataManager {
    public PlayerProgressManager(Player player) {
        super(player);
    }

    /**
     * Handler for player login.
     * */
    public void onPlayerLogin() {
        // Try unlocking open states on player login. This handles accounts where unlock conditions were
        // already met before certain open state unlocks were implemented.
        this.tryUnlockOpenStates(false);

        // Send notify to the client.
        player.getSession().send(new PacketOpenStateUpdateNotify(this.player));

        // Add statue quests if necessary.
        this.addStatueQuestsOnLogin();

        // Auto-unlock the first statue and map area, until we figure out how to make
        // that particular statue interactable.
        this.player.getUnlockedScenePoints(3).add(7);
        this.player.getUnlockedSceneAreas(3).add(1);

        // add replacement costumes if necessary
        this.addReplaceCostumes();

    }

    /**
     * OPEN STATES
     * */
    // Set of open states that are never unlocked, whether they fulfill the conditions or not.
    public static final Set<Integer> BLACKLIST_OPEN_STATES = Set.of(
    48      // blacklist OPEN_STATE_LIMIT_REGION_GLOBAL to make Meledy happy. =D Remove this as soon as quest unlocks are fully implemented.
    );

    // Set of open states that are set per default for all accounts. Can be overwritten by an entry in `map`.
    public static final Set<Integer> DEFAULT_OPEN_STATES = GameData.getOpenStateList().stream()
        .filter(s ->
            s.isDefaultState() && !s.isAllowClientOpen() // Actual default-opened states.
            || (s.getCond().stream().filter(cond -> cond.getCondType() == OpenStateCondType.OPEN_STATE_COND_PLAYER_LEVEL)
                .allMatch(cond -> cond.getParam() == 1))
            // All states whose unlock we don't handle correctly yet.
            || (s.getCond().stream().anyMatch(c ->
                    c.getCondType() == OpenStateCondType.OPEN_STATE_OFFERING_LEVEL
                    || c.getCondType() == OpenStateCondType.OPEN_STATE_CITY_REPUTATION_LEVEL))
            // Always unlock OPEN_STATE_PAIMON, otherwise the player will not have a working chat.
            || s.getId() == 1
        )
        .map(OpenStateData::getId)    // Filter out states in the blacklist.
        .filter(id -> !BLACKLIST_OPEN_STATES.contains(id))
        .collect(Collectors.toSet());

    /**
     * Direct getters and setters for open states.
     * */
    public int getOpenState(int openState) {
        return this.player.getOpenStates().getOrDefault(openState, 0);
    }

    private void setOpenState(int openState, int value, boolean sendNotify) {
        int previousValue = this.player.getOpenStates().getOrDefault(openState, 0);
        if (value == previousValue) return;

        this.player.getOpenStates().put(openState, value);
        this.player.getQuestManager().queueEvent(QuestCond.QUEST_COND_OPEN_STATE_EQUAL, openState, value);

        if (sendNotify) {
            player.getSession().send(new PacketOpenStateChangeNotify(openState, value));
        }
    }
    private void setOpenState(int openState, int value) {
        this.setOpenState(openState, value, true);
    }

    /**********
        Condition checking for setting open states.
    **********/
    private boolean areConditionsMet(OpenStateData openState) {
        // Check all conditions and test if at least one of them is violated.
        return openState.getCond().stream().allMatch(condition -> switch (condition.getCondType()){
            // For level conditions, check if the player has reached the necessary level.
            case OPEN_STATE_COND_PLAYER_LEVEL -> this.player.getLevel() >= condition.getParam();
            // check sub quest id for quest finished met requirements
            case OPEN_STATE_COND_QUEST -> Optional.ofNullable(this.player.getQuestManager().getQuestById(condition.getParam()))
                    .map(GameQuest::getState).filter(state -> state == QuestState.QUEST_STATE_FINISHED)
                    .isPresent();
            // check main quest id for quest finished met requirements, TODO not sure if its having or finished quest
            case OPEN_STATE_COND_PARENT_QUEST -> Optional.ofNullable(this.player.getQuestManager().getMainQuestById(condition.getParam()))
                    .map(GameMainQuest::getState).filter(state -> state == ParentQuestState.PARENT_QUEST_STATE_FINISHED)
                    .isPresent();
            // ToDo: Implement.
            case OPEN_STATE_OFFERING_LEVEL, OPEN_STATE_CITY_REPUTATION_LEVEL -> true;
        });
    }

    /**********
        Setting open states from the client (via `SetOpenStateReq`).
    **********/
    public void setOpenStateFromClient(int openState, int value) {
        // Get the data for this open state.
        OpenStateData data = GameData.getOpenStateDataMap().get(openState);

        // Make sure that this is an open state that the client is allowed to set,
        // and that it doesn't have any further conditions attached.
        if (data == null || !data.isAllowClientOpen() || !this.areConditionsMet(data)) {
            this.player.sendPacket(new PacketSetOpenStateRsp(Retcode.RET_FAIL));
            return;
        }

        // Set.
        this.setOpenState(openState, value);
        this.player.sendPacket(new PacketSetOpenStateRsp(openState, value));
    }

    /**
     * This force sets an open state, ignoring all conditions and permissions
     */
    public void forceSetOpenState(int openState, int value){
        setOpenState(openState, value);
    }

    /**********
        Triggered unlocking of open states (unlock states whose conditions have been met.)
    **********/
    public void tryUnlockOpenStates(boolean sendNotify) {
        // TODO probably better to build similar structure as quest handler
        // so that it doesn't have to loop through all the states and check
        // To auto-unlock a state, it has to meet three conditions:
        // * it can not be a state that is unlocked by the client,
        // * it has to meet all its unlock conditions, and
        // * it can not be in the blacklist.
        GameData.getOpenStateList().stream()
            // Get list of open states that are not yet unlocked.
            .filter(s -> Optional.ofNullable(this.player.getOpenStates().get(s.getId())).isPresent())
            .filter(s -> !s.isAllowClientOpen())
            .filter(this::areConditionsMet)
            .filter(state -> !BLACKLIST_OPEN_STATES.contains(state.getId()))
            .forEach(state -> setOpenState(state.getId(), 1, sendNotify));
    }
    public void tryUnlockOpenStates() {
        this.tryUnlockOpenStates(true);
    }

    /**
     * MAP AREAS AND POINTS
     * */
    private void addStatueQuestsOnLogin() {
        // Add the main statue quest if it isn't active yet.
        val statueGameMainQuest = Optional.ofNullable(this.player.getQuestManager().getMainQuestById(303))
            .or(() -> Optional.ofNullable(this.player.getQuestManager().addQuest(30302)).map(GameQuest::getMainQuest))
            .orElse(null);

        // Get all currently existing sub quests for the "unlock all statues" main quest.
        // Set all sub quests to active if they aren't already finished.
        Arrays.stream(GameData.getMainQuestDataMap().get(303).getSubQuests())
            .map(SubQuestData::getSubId)
            .map(subQuestId -> Optional.ofNullable(statueGameMainQuest).map(q -> q.getChildQuestById(subQuestId)).orElse(null))
            .filter(subGameQuest -> Optional.ofNullable(subGameQuest).map(GameQuest::getState)
                .filter(state -> state == QuestState.QUEST_STATE_UNSTARTED).isPresent())
            .map(GameQuest::getQuestData)
            .forEach(this.player.getQuestManager()::addQuest);
    }

    public boolean unlockTransPoint(int sceneId, int pointId, boolean isStatue) {
        // Check whether the unlocked point exists and whether it is still locked.
        ScenePointEntry scenePointEntry = GameData.getScenePointEntryById(sceneId, pointId);
        if (scenePointEntry == null || this.player.getUnlockedScenePoints(sceneId).contains(pointId)) return false;

        // Add the point to the list of unlocked points for its scene.
        this.player.getUnlockedScenePoints(sceneId).add(pointId);

        // Give primogems  and Adventure EXP for unlocking.
        this.player.getInventory().addItem(201, 5, ActionReason.UnlockPointReward);
        this.player.getInventory().addItem(102, isStatue ? 50 : 10, ActionReason.UnlockPointReward);

        // this.player.sendPacket(new PacketPlayerPropChangeReasonNotify(this.player.getProperty(PlayerProperty.PROP_PLAYER_EXP), PlayerProperty.PROP_PLAYER_EXP, PropChangeReason.PROP_CHANGE_REASON_PLAYER_ADD_EXP));

        // Fire quest and script trigger for trans point unlock.
        this.player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_UNLOCK_TRANS_POINT, sceneId, pointId);
        this.player.getScene().getScriptManager().callEvent(new ScriptArgs(0, EVENT_UNLOCK_TRANS_POINT, sceneId, pointId));

        // Send packet.
        this.player.sendPacket(new PacketScenePointUnlockNotify(sceneId, pointId));
        return true;
    }

    public void unlockSceneArea(int sceneId, int areaId) {
        // Add the area to the list of unlocked areas in its scene.
        this.player.getUnlockedSceneAreas(sceneId).add(areaId);

        // Send packet.
        this.player.sendPacket(new PacketSceneAreaUnlockNotify(sceneId, areaId));
        this.player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_UNLOCK_AREA, sceneId, areaId);
    }

    /**
     * Give replace costume to player (Ambor, Jean, Mona, Rosaria)
     */
    public void addReplaceCostumes(){
        GameData.getAvatarReplaceCostumeDataMap().values().stream()
            .map(AvatarReplaceCostumeData::getCostumeId)
            .filter(costumeId -> Optional.ofNullable(GameData.getAvatarCostumeDataMap().get(costumeId.intValue())).isPresent())
            .filter(costumeId -> !this.player.getCostumeList().contains(costumeId))
            .forEach(this.player::addCostume);
    }

    /**
     * Quest progress
     */
    public void addQuestProgress(int id, int count){
        this.player.save();
        this.player.getQuestManager().queueEvent(
            QuestContent.QUEST_CONTENT_ADD_QUEST_PROGRESS, id, this.player.getPlayerProgress().addToCurrentProgress(id, count));
    }

    /**
     * Item history
     */
    public void addItemObtainedHistory(int id, int count){
        this.player.save();
        this.player.getQuestManager().queueEvent(
            QuestCond.QUEST_COND_HISTORY_GOT_ANY_ITEM, id, this.player.getPlayerProgress().addToItemHistory(id, count));
    }
}
