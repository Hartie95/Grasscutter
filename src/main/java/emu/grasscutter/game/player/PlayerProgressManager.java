package emu.grasscutter.game.player;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.ScenePointEntry;
import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.excels.CityData;
import emu.grasscutter.data.excels.CityLevelUpData;
import emu.grasscutter.data.excels.OpenStateData;
import emu.grasscutter.data.excels.OpenStateData.*;
import emu.grasscutter.data.excels.RewardData;
import emu.grasscutter.data.excels.TransPointRewardData;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.quest.enums.ParentQuestState;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.quest.enums.QuestState;
import emu.grasscutter.net.proto.CityInfoOuterClass.CityInfo;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.packet.send.PacketLevelUpCityRsp;
import emu.grasscutter.server.packet.send.PacketOpenStateChangeNotify;
import emu.grasscutter.server.packet.send.PacketOpenStateUpdateNotify;
import emu.grasscutter.server.packet.send.PacketSceneAreaUnlockNotify;
import emu.grasscutter.server.packet.send.PacketScenePointUnlockNotify;
import emu.grasscutter.server.packet.send.PacketSetOpenStateRsp;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import static emu.grasscutter.game.quest.enums.QuestCond.QUEST_COND_OPEN_STATE_EQUAL;
import static emu.grasscutter.scripts.constants.EventType.EVENT_UNLOCK_TRANS_POINT;

// @Entity
public class PlayerProgressManager extends BasePlayerDataManager {
    public PlayerProgressManager(Player player) {
        super(player);
    }

    /**********
        Handler for player login.
    **********/
    public void onPlayerLogin() {
        // Try unlocking open states on player login. This handles accounts where unlock conditions were
        // already met before certain open state unlocks were implemented.
        this.tryUnlockOpenStates(false);
        this.tryAddingCity();
        player.getSession().send(new PacketOpenStateUpdateNotify(this.player));
    }

    /******************************************************************************************************************
     ******************************************************************************************************************
     * OPEN STATES
     ******************************************************************************************************************
     *****************************************************************************************************************/

    // Set of open states that are never unlocked, whether they fulfill the conditions or not.
    public static final Set<Integer> BLACKLIST_OPEN_STATES = Set.of(
    48      // blacklist OPEN_STATE_LIMIT_REGION_GLOBAL to make Meledy happy. =D Remove this as soon as quest unlocks are fully implemented.
    );

    // Set of open states that are set per default for all accounts. Can be overwritten by an entry in `map`.
    public static final Set<Integer> DEFAULT_OPEN_STATES = GameData.getOpenStateList().stream()
        .filter(s ->
            (s.isDefaultState()      // Actual default-opened states.
            && !s.isAllowClientOpen())
            || ((s.getCond().size() == 1)
            && (s.getCond().get(0).getCondType() == OpenStateCondType.OPEN_STATE_COND_PLAYER_LEVEL)
            && (s.getCond().get(0).getParam() == 1))
        )
        .filter(s -> !BLACKLIST_OPEN_STATES.contains(s.getId()))    // Filter out states in the blacklist.
        .map(s -> s.getId())
        .collect(Collectors.toSet());

    /**********
        Direct getters and setters for open states.
    **********/
    public int getOpenState(int openState) {
        return this.player.getOpenStates().getOrDefault(openState, 0);
    }

    private void setOpenState(int openState, int value, boolean sendNotify) {
        int previousValue = this.player.getOpenStates().getOrDefault(openState, 0);

        if (value != previousValue) {
            this.player.getOpenStates().put(openState, value);

            this.player.getQuestManager().queueEvent(QUEST_COND_OPEN_STATE_EQUAL, openState, value);

            if (sendNotify) {
                player.getSession().send(new PacketOpenStateChangeNotify(openState, value));
            }
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
        for (val condition : openState.getCond()) {
            // For level conditions, check if the player has reached the necessary level.
            if (condition.getCondType() == OpenStateCondType.OPEN_STATE_COND_PLAYER_LEVEL) {
                if (this.player.getLevel() < condition.getParam()) {
                    return false;
                }
            } else if (condition.getCondType() == OpenStateCondType.OPEN_STATE_OFFERING_LEVEL) {
                // ToDo: Implement.
                return false;
            } else if (condition.getCondType() == OpenStateCondType.OPEN_STATE_CITY_REPUTATION_LEVEL) {
                // ToDo: Implement.
                return false;
            } else if (condition.getCondType() == OpenStateCondType.OPEN_STATE_COND_QUEST) {
                // check sub quest id for quest finished met requirements
                val quest = this.player.getQuestManager().getQuestById(condition.getParam());
                if (quest == null
                    || quest.getState() != QuestState.QUEST_STATE_FINISHED){
                    return false;
                }
            } else if (condition.getCondType() == OpenStateCondType.OPEN_STATE_COND_PARENT_QUEST) {
                // check main quest id for quest finished met requirements
                // TODO not sure if its having or finished quest
                val mainQuest = this.player.getQuestManager().getMainQuestById(condition.getParam());
                if (mainQuest == null
                    || mainQuest.getState() != ParentQuestState.PARENT_QUEST_STATE_FINISHED){
                    return false;
                }
            }
        }

        // Done. If we didn't find any violations, all conditions are met.
        return true;
    }

    /**********
        Setting open states from the client (via `SetOpenStateReq`).
    **********/
    public void setOpenStateFromClient(int openState, int value) {
        // Get the data for this open state.
        OpenStateData data = GameData.getOpenStateDataMap().get(openState);
        if (data == null) {
            this.player.sendPacket(new PacketSetOpenStateRsp(Retcode.RET_FAIL));
            return;
        }

        // Make sure that this is an open state that the client is allowed to set,
        // and that it doesn't have any further conditions attached.
        if (!data.isAllowClientOpen() || !this.areConditionsMet(data)) {
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
        // Get list of open states that are not yet unlocked.
        List<OpenStateData> lockedStates = GameData.getOpenStateList().stream()
            .filter(s -> this.player.getOpenStates().getOrDefault(s, 0) == 0).toList();

        // Try unlocking all of them.
        for (OpenStateData state : lockedStates) {
            // TODO probably better to build similar structure as quest handler
            // so that it doesnt have to loop through all the states and check
            // To auto-unlock a state, it has to meet three conditions:
            // * it can not be a state that is unlocked by the client,
            // * it has to meet all its unlock conditions, and
            // * it can not be in the blacklist.
            if (!state.isAllowClientOpen() && this.areConditionsMet(state) && !BLACKLIST_OPEN_STATES.contains(state.getId())) {
                this.setOpenState(state.getId(), 1, sendNotify);
            }
        }
    }

    public void tryUnlockOpenStates() {
        this.tryUnlockOpenStates(true);
    }

    public boolean unlockTransPoint(int sceneId, int pointId) {
        // Check whether the unlocked point exists and whether it is still locked.
        ScenePointEntry scenePointEntry = GameData.getScenePointEntryById(sceneId, pointId);

        if (scenePointEntry == null || this.player.getUnlockedScenePoints(sceneId).contains(pointId)) {
            return false;
        }

        // Add the point to the list of unlocked points for its scene.
        this.player.getUnlockedScenePoints(sceneId).add(pointId);

        // Give primogems and Adventure EXP for unlocking.
        TransPointRewardData reward = GameData.getTransPointRewardDataMap().get((sceneId << 16) + pointId);
        if (reward != null) {
            RewardData rewardData = GameData.getRewardDataMap().get(reward.getRewardId());
            if (rewardData != null) {
                if (rewardData.getHcoin() > 0) {
                    this.player.getInventory().addItem(201, rewardData.getHcoin(), ActionReason.UnlockPointReward);
                }

                if (rewardData.getPlayerExp() > 0) {
                    this.player.getInventory().addItem(102, rewardData.getPlayerExp(), ActionReason.UnlockPointReward);
                }

                if (!rewardData.getRewardItemList().isEmpty()) { // there is a event that gives item for unlocking point
                    this.player.getInventory().addItemParamDatas(rewardData.getRewardItemList(), ActionReason.UnlockPointReward);
                }
            }
        }

        // Fire quest and script trigger for trans point unlock.
        this.player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_UNLOCK_TRANS_POINT, sceneId, pointId);
        this.player.getScene().getScriptManager().callEvent(new ScriptArgs(EVENT_UNLOCK_TRANS_POINT, sceneId, pointId));

        // Send packet.
        this.player.sendPacket(new PacketScenePointUnlockNotify(sceneId, pointId, true));
        return true;
    }

    public void unlockSceneArea(int sceneId, int areaId) {
        // Add the area to the list of unlocked areas in its scene.
        this.player.getUnlockedSceneAreas(sceneId).add(areaId);
        this.getCityLevel(sceneId, areaId);
        // Send packet.
        this.player.sendPacket(new PacketSceneAreaUnlockNotify(sceneId, areaId));
        this.player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_UNLOCK_AREA, sceneId, areaId);
    }

    private void tryAddingCity() {
        this.player.getUnlockedSceneAreas().entrySet().stream().forEach(e -> 
            getCityLevel(e.getKey(), e.getValue().stream().findFirst().orElse(-1))
        );
    }
    
    private int getCityLevel(int sceneId, int areaId) {
        return getCityLevel(GameData.getCityBySceneArea(sceneId, areaId));
    }

    private int getCityLevel(int cityId) {
        if (cityId <= 0) return -1; 
        return this.player.getCityLevelMap().computeIfAbsent(cityId, s -> CityInfoItem.create()).getLevel();
    }

    private int getCityExp(int cityId) {
        if (cityId <= 0) return -1; 
        return this.player.getCityLevelMap().computeIfAbsent(cityId, s -> CityInfoItem.create()).getExp();
    }

    private int getCrystalIdByCity(int cityId) {
        return GameData.getLevelUpDataByCity(cityId).stream()
            .map(data -> data.getConsumeItem().getId())
            .findFirst()
            .orElse(0);
    }

    public void levelUpCity(int sceneId, int areaId, int itemNum) {
        int cityId = GameData.getCityBySceneArea(sceneId, areaId);

        if (cityId == 0 || getCityLevel(cityId) >= 10) return;

        // pay all item
        this.player.getInventory().payItem(this.getCrystalIdByCity(cityId), itemNum); // TODO action reason
        int[] itemNumCounter = new int[]{itemNum};

        GameData.getLevelUpDataByCity(cityId).stream()
            .filter(x -> x.getLevel() >= getCityLevel(cityId)+1)
            .sorted(Comparator.comparingInt(CityLevelUpData::getLevel))
            .takeWhile(x -> itemNumCounter[0] > 0) 
            .forEach(data -> {
                RewardData rewardData = GameData.getRewardDataMap().get(data.getRewardID());
                if (rewardData == null) return;

                this.player.getInventory().addItemParamDatas(rewardData.getRewardItemList(), ActionReason.CityLevelupReward);
                this.player.getCityLevelMap().get(cityId).addExp(itemNumCounter[0], data.getConsumeItem().getCount());
                itemNumCounter[0] -= data.getConsumeItem().getCount();
                this.player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_CITY_LEVEL_UP, cityId, getCityLevel(cityId));
                // TODO, world action
            });
        this.player.sendPacket(new PacketLevelUpCityRsp(areaId, sceneId, cityInfoToProto(cityId)));
    }


    /**
     * Quest progress
     */

    public void addQuestProgress(int id, int count){
        // if quest key do not exists, put param[1] as value
        // otherwise sum 1 to the value linked to key
        val newCount = player.getQuestProgressCountMap().merge(id, count, Integer::sum);
        player.save();
        player.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_ADD_QUEST_PROGRESS, id, newCount);
    }

    public CityInfo cityInfoToProto(int cityId) {
        return CityInfo.newBuilder()
            .setCityId(cityId)
            .setCrystalNum(getCityExp(cityId))
            .setLevel(getCityLevel(cityId))
            .build();
    }
}
