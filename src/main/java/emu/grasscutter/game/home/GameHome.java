package emu.grasscutter.game.home;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Transient;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.HomeworldDefaultSaveData.*;
import emu.grasscutter.data.excels.FurnitureMakeConfigData;
import emu.grasscutter.data.excels.HomeWorldComfortLevelData;
import emu.grasscutter.data.excels.HomeWorldLevelData;
import emu.grasscutter.data.excels.HomeWorldLimitShopData;
import emu.grasscutter.data.excels.RewardData;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.net.proto.FurnitureMakeDataOuterClass.FurnitureMakeData;
import emu.grasscutter.net.proto.HomeBlockArrangementInfoOuterClass.HomeBlockArrangementInfo;
import emu.grasscutter.net.proto.HomeLimitedShopInfoOuterClass.HomeLimitedShopInfo;
import emu.grasscutter.net.proto.HomeResourceOuterClass.HomeResource;
import emu.grasscutter.net.proto.HomeSceneArrangementInfoOuterClass.HomeSceneArrangementInfo;
import emu.grasscutter.server.packet.send.*;
import emu.grasscutter.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Entity(value = "homes", useDiscriminator = false)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class GameHome {
    @Id
    String id;

    @Indexed(options = @IndexOptions(unique = true))
    long ownerUid;
    @Transient Player player;

    int level;
    int exp;
    int highestComfort;
    int fetterExpStoreValue;
    int fetterExpRefreshTime;
    int homeCoinStoreValue;
    int homeCoinRefreshTime;
    HomeWeeklyDjinItem weeklyDjin;
    List<FurnitureMakeSlotItem> furnitureMakeSlotItemList;
    // 1st integer: realm id, 2nd integer: sceneid
    // to prevent home furniture from different realm being saved to the same house
    ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, HomeSceneItem>> realmBlockMap;
    // plant status
    ConcurrentHashMap<Integer, HomePlantItem> realmPlantMap;
    Set<Integer> unlockedHomeBgmList;
    int enterHomeOption;

    public void save() {
        DatabaseHelper.saveHome(this);
    }

    public static GameHome getByUid(Integer uid) {
        GameHome home = DatabaseHelper.getHomeByUid(uid);
        if (home == null) {
            home = GameHome.create(uid);
        }
        return home;
    }

    public static GameHome create(Integer uid) {
        return GameHome.of()
            .ownerUid(uid)
            .level(1)
            .furnitureMakeSlotItemList(new ArrayList<>())
            .realmBlockMap(new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, HomeSceneItem>>())
            .realmPlantMap(new ConcurrentHashMap<Integer, HomePlantItem>())
            .unlockedHomeBgmList(new HashSet<>())
            .weeklyDjin(HomeWeeklyDjinItem.create())
            .build();
    }

    public int getRealmSceneIdByModule(int moduleId) {
        return GameData.getHomeWorldModuleDataMap().get(moduleId).getWorldSceneId();
    }

    public int getCurrentRealmSceneId() {
        return getRealmSceneIdByModule(getPlayer().getCurrentRealmId());
    }
    
    public HomeSceneItem getHomeSceneItem() {
        return getHomeSceneItem(getCurrentRealmSceneId());
    }

    public HomeSceneItem getHomeSceneItem(int sceneId) {
        return getHomeSceneItem(getPlayer().getCurrentRealmId(), sceneId);
    }

    public HomeSceneItem getHomeSceneItem(int moduleId, int sceneId) {
        if (realmBlockMap.get(moduleId) == null) {
            realmBlockMap.put(moduleId, new ConcurrentHashMap<>());
        }
        return realmBlockMap.get(moduleId).computeIfAbsent(sceneId, e -> {
            var defaultItem = GameData.getHomeworldDefaultSaveData().get(sceneId);
            if (defaultItem != null) {
                Grasscutter.getLogger().info("Set player {} home {} to initial setting", ownerUid, sceneId);
                return HomeSceneItem.parseFrom(defaultItem, sceneId);
            }
            return null;
        });
    }

    public HomePlantItem getHomePlantItem() {
        return realmPlantMap.get(getPlayer().getCurrentRealmId());
    }

    public void onOwnerLogin(Player player) {
        if (this.player == null)
            this.player = player;
        sendInitialPackets();
    }

    public void sendInitialPackets() {
        player.sendPacket(new PacketHomeResourceNotify(getPlayer()));
        player.sendPacket(new PacketPlayerHomeCompInfoNotify(getPlayer()));
        player.sendPacket(new PacketHomeMarkPointNotify(getPlayer()));
        player.sendPacket(new PacketHomeBasicInfoNotify(getPlayer(), false));
        player.sendPacket(new PacketHomeComfortInfoNotify(getPlayer()));
        player.sendPacket(new PacketHomePlantInfoNotify(getPlayer()));
        // HomeAvatarTalkFinishInfoNotify
        // HomeAvatarRewardEventNotify
        // HomeAvatarAllFinishRewardNotify
        // HomePriorCheckNotify
        player.sendPacket(new PacketFurnitureCurModuleArrangeCountNotify());
        player.sendPacket(new PacketUnlockedHomeBgmNotify(getPlayer()));
    }

    public Player getPlayer() {
        if (this.player == null)
            this.player = Grasscutter.getGameServer().getPlayerByUid((int) this.ownerUid, true);
        return this.player;
    }

    public HomeWorldLevelData getLevelData() {
        return GameData.getHomeWorldLevelDataMap().get(level);
    }

    public HomeWorldComfortLevelData getComfortLevelData() {
        return GameData.getHomeWorldComfortLevelDataMap().get(
            GameData.getHomeWorldComfortLevelDataMap().keySet().stream().min((f1,f2) -> f1.compareTo(getHighestComfort())).get()
        );
    }

    public boolean addUnlockedHomeBgm(int homeBgmId) {
        if (!getUnlockedHomeBgmList().add(homeBgmId)) return false;

        getPlayer().sendPacket(new PacketUnlockHomeBgmNotify(homeBgmId));
        getPlayer().sendPacket(new PacketUnlockedHomeBgmNotify(getPlayer()));
        save();
        return true;
    }

    public Set<Integer> getUnlockedHomeBgmList() {
        if (this.unlockedHomeBgmList == null) {
            this.unlockedHomeBgmList = new HashSet<>();
        }

        if (this.unlockedHomeBgmList.addAll(getDefaultUnlockedHomeBgmIds())) {
            save();
        }

        return this.unlockedHomeBgmList;
    }

    private Set<Integer> getDefaultUnlockedHomeBgmIds() {
        return GameData.getHomeWorldBgmDataMap().int2ObjectEntrySet().stream()
            .filter(e -> e.getValue().isDefaultUnlock())
            .map(Int2ObjectMap.Entry::getIntKey)
            .collect(Collectors.toUnmodifiableSet());
    }

    public void updateHomeSceneBlock(HomeSceneArrangementInfo arrangementInfo) {
        HomeSceneItem homeScene = getHomeSceneItem(arrangementInfo.getSceneId());
        homeScene.update(arrangementInfo);

        HomePlantItem newPlantItemList = arrangementInfo.getBlockArrangementInfoListList().stream()
            .map(HomeBlockArrangementInfo::getFieldListList)
            .map(fieldList -> HomePlantItem.parseFrom(arrangementInfo.getSceneId(), fieldList))
            .findAny()
            .orElse(null);

        // if newly updated arrangement doesn't have plant block
        if (newPlantItemList == null) {
            // remove all saved plant 
            if (getRealmPlantMap().get(getPlayer().getCurrentRealmId()) != null) {
                getRealmPlantMap().remove(getPlayer().getCurrentRealmId());
            }
            return;
        }

        // if newly updated arrangement has plant block but dont have saved plant item 
        if (getRealmPlantMap().get(getPlayer().getCurrentRealmId()) == null) {
            getRealmPlantMap().put(getPlayer().getCurrentRealmId(), newPlantItemList);
            return;
        }

        // update plant item, meaning add item from updated item and remove
        // saved item that updated item dont have
        getRealmPlantMap().get(getPlayer().getCurrentRealmId()).update(newPlantItemList.getFieldList());
    }

    public void updateFetterExpResource() {
        // initial setting
        if (getFetterExpRefreshTime() == 0) {
            // set refresh time to next hour
            setFetterExpRefreshTime(Utils.getCurrentSeconds() + 3600);
            return;
        }
        // dont update if next refresh time is within one hour of last retrieve
        if (Utils.getCurrentSeconds() < getFetterExpRefreshTime()) return;

        // should start calculating from one hour before refresh time
        double refreshTimeInterval = (double) Math.abs(Utils.getCurrentSeconds() - getFetterExpRefreshTime() + 3600);
        int time = (refreshTimeInterval - 3600) < 3600 ? 3600 : (int) Math.abs(refreshTimeInterval - 3600);
        
        // next refresh time should compensate for the hour before last refresh time 
        // and next refresh time in refreshTimeInterval
        setFetterExpRefreshTime(getFetterExpRefreshTime() + time);
        int gainedFetterExp = getFetterExpStoreValue() + (int) Math.floor((refreshTimeInterval / 3600) * getComfortLevelData().getCompanionshipExpProduceRate());
        // if gained fetter exceed store limit, cap at store limit
        setFetterExpStoreValue(gainedFetterExp < getLevelData().getHomeFetterExpStoreLimit() ? gainedFetterExp : getLevelData().getHomeFetterExpStoreLimit());
    }

    public void updateHomeCoinResource() {
        // initial setting
        if (getHomeCoinRefreshTime() == 0) {
            // set refresh time to next hour
            setHomeCoinRefreshTime(Utils.getCurrentSeconds() + 3600);
            return;
        }
        // dont update if its not the time yet
        if (Utils.getCurrentSeconds() < getHomeCoinRefreshTime()) return;

        // should start calculating from one hour before refresh time
        double refreshTimeInterval = (double) Math.abs(Utils.getCurrentSeconds() - getHomeCoinRefreshTime() + 3600);        // if previous refresh interval less than an hour, set new refresh time to next hour
        int time = (refreshTimeInterval - 3600) < 3600 ? 3600 : (int) Math.abs(refreshTimeInterval - 3600);

        // set next refresh time to the next closest hour of previous refresh time
        setHomeCoinRefreshTime(getHomeCoinRefreshTime() + time);
        int gainedHomeCoin = getHomeCoinStoreValue() + ((int) Math.floor(refreshTimeInterval) / 3600) * getComfortLevelData().getHomeCoinProduceRate();
        // if gained home coin exceed store limit, cap at store limit
        setHomeCoinStoreValue(gainedHomeCoin < getLevelData().getHomeCoinStoreLimit() ? gainedHomeCoin : getLevelData().getHomeCoinStoreLimit());
    }

    public void updateResources() {
        updateFetterExpResource();
        updateHomeCoinResource();
        save();
    }

    public void receiveFetterExp() {
        // player must be in serenitea pot to get fetter level
        // get currently selected realm
        HomeSceneItem homeScene = getHomeSceneItem();
        // give fetter exp to all deployed avatar in home scene (exterior)
        homeScene.getBlockItems().values().stream()
            .forEach(block -> block.upgradeAllNPCFetterLevel(getPlayer(), getFetterExpStoreValue()));
            
        // give fetter exp to all deployed avatar in room scene (interior)
        getHomeSceneItem(homeScene.getRoomSceneId()).getBlockItems().values().stream()
            .forEach(block -> block.upgradeAllNPCFetterLevel(getPlayer(), getFetterExpStoreValue()));

        setFetterExpStoreValue(0);
        setFetterExpRefreshTime(Utils.getCurrentSeconds() + 3600);
    }

    public boolean receiveHomeCoin() {
        // if no coin to be received
        if (getHomeCoinStoreValue() == 0) return false;
        
        getPlayer().getInventory().addItem(204, getHomeCoinStoreValue(), ActionReason.HomeCoinCollect);
        setHomeCoinStoreValue(0);
        setHomeCoinRefreshTime(Utils.getCurrentSeconds() + 3600);
        return true;
    }

    public void addExp(int exp) {
        // if the added exp does not exceed limit, add it
        if (getLevelData().getExp() != 0 && getLevelData().getExp() > (getExp() + exp)) {
            setExp(getExp() + exp);
            return;
        }

        // if not reach the max level
        if (GameData.getHomeWorldLevelDataMap().get(getLevel() + 1) != null) {
            // add the remaining exp to the new level
            if (getLevelData().getExp() != 0) {
                setExp(getExp() + exp - getLevelData().getExp());
            }
            // upgrade level
            setLevel(getLevel() + 1);
        }
    }

    public boolean furnitureShouldGiveExp(int furnitureMakeId) {
        getPlayer().getMadeFurniture().merge(furnitureMakeId, 1, Integer::sum);
        return getPlayer().getMadeFurniture().getOrDefault(furnitureMakeId, 0) == 1;
    }

    public boolean getLevelUpReward(int level) {
        // if already taken, which probably wont be sent by client, but just 
        // to be safe if used somewhere else 
        if (getPlayer().getTeapotLevelReward().contains(level)) return false;

        int rewardId = GameData.getHomeWorldLevelDataMap().get(level).getRewardId();
        RewardData rewardData = GameData.getRewardDataMap().get(rewardId);
        if (rewardData == null) return false;

        getPlayer().getTeapotLevelReward().add(level);
        getPlayer().getInventory().addItemParamDatas(rewardData.getRewardItemList(), ActionReason.GetHomeLevelupReward);
        return true;
    }

    public int getLastMakingSlot() {
        return getFurnitureMakeSlotItemList().stream()
            .filter(slotItem -> slotItem.getMakeId() != 0)
            .map(FurnitureMakeSlotItem::getIndex)
            .max(Integer::compare)
            .orElse(-1);
    }

    public void updateMakingSlotIndex(int index) {
        getFurnitureMakeSlotItemList().stream()
            .filter(slotItem -> slotItem.getIndex() > index && slotItem.getMakeId() != 0)
            .forEach(slotItem -> slotItem.setIndex(slotItem.getIndex()-1));
    }

    public HomeWeeklyDjin getWeeklyDjinData(int appearBlock) {
        return GameData.getHomeworldDefaultSaveData().get(getCurrentRealmSceneId()).getHomeBlockLists().get(appearBlock).getWeeklyDjin().get(0);
    }

    public List<HomeWorldLimitShopData> getLimitShopGoodsByPool(int poolId) {
        // TODO if there is a better way to do shuffling, please replace this
        List<HomeWorldLimitShopData> goodsInPool = GameData.getHomeWorldLimitShopDataMap().values().stream()
            .filter(x -> x.getPoolID() == poolId)
            .toList();

        List<HomeWorldLimitShopData> randomGoods = new ArrayList<>();
        for (int i = 0; i < 2; i++) { // get two random goods from pool
            int randomIndex = Utils.randomRange(0, goodsInPool.size()-1);
            randomGoods.add(goodsInPool.get(randomIndex));
        }
        return randomGoods;
    }

    public void refreshLimitedShop() {
        // if (getWeeklyDjin().getGoods() == null) getWeeklyDjin().setGoods(new ConcurrentHashMap<Integer, Integer>());
        getWeeklyDjin().getGoods().clear();
        
        Set<Integer> pools = GameData.getHomeWorldLimitShopDataMap().values().stream()
            .map(HomeWorldLimitShopData::getPoolID)
            .collect(Collectors.toSet());
            
        pools.stream().forEach(poolId -> {
            List<HomeWorldLimitShopData> goodsData = getLimitShopGoodsByPool(poolId);
            if (goodsData == null || goodsData.size() < 2) return;

            goodsData.stream().forEach(goods -> {
                getWeeklyDjin().getGoods().put(goods.getGoodsId(), 0);
            });
        });
    }

    public void updateWeeklyDjin() {
        getWeeklyDjin().updatePos(
            getWeeklyDjin().getAppearBlock(), 
            getWeeklyDjinData(getWeeklyDjin().getAppearBlock()-1).getPos(), 
            getWeeklyDjinData(getWeeklyDjin().getAppearBlock()-1).getRot());

        // if weekly djin doesnt need to update
        if (Utils.getCurrentSeconds() < getWeeklyDjin().getNextCloseTime()) return;

        if (getWeeklyDjin() == null) setWeeklyDjin(HomeWeeklyDjinItem.create());
        // update djin position every time in case player change realm
        int appearInBlock = Utils.randomRange(1, getLevelData().getOutdoorUnlockBlockCount());
        getWeeklyDjin().updatePos(
            appearInBlock, 
            getWeeklyDjinData(appearInBlock-1).getPos(), 
            getWeeklyDjinData(appearInBlock-1).getRot());

        refreshLimitedShop();
        // TODO feel like it can be simplified more 
        if (LocalDateTime.now().getHour() >= 4) {
            switch (LocalDate.now().getDayOfWeek()) {
                case FRIDAY:
                    getWeeklyDjin().setAllTime(true, false, true);
                    break;
                case SATURDAY:
                case SUNDAY:
                    getWeeklyDjin().setAllTime(true, false, false);
                    break;
                default: 
                    getWeeklyDjin().setAllTime(true, true, true);
            }
            return;
        }

        switch (LocalDate.now().getDayOfWeek()) {
            case MONDAY:
            case SUNDAY:
                getWeeklyDjin().setAllTime(true, false, false);
                break;
            case SATURDAY:
                getWeeklyDjin().setAllTime(true, false, true);
                break;
            default: 
                getWeeklyDjin().setAllTime(true, true, true);
        }       
    }

    public HomeResource fetterExpResourceToProto() {
        return HomeResource.newBuilder()
            .setStoreLimit(getLevelData().getHomeFetterExpStoreLimit())
            .setStoreValue(getFetterExpStoreValue())
            .setNextRefreshTime(getFetterExpRefreshTime())
            .build();
    }

    public HomeResource homeCoinResourceToProto() {
        return HomeResource.newBuilder()
            .setStoreLimit(getLevelData().getHomeCoinStoreLimit())
            .setStoreValue(getHomeCoinStoreValue())
            .setNextRefreshTime(getHomeCoinRefreshTime())
            .build();
    }

    public List<FurnitureMakeData> furnitureMakeItemToProto() {
        return getFurnitureMakeSlotItemList().stream()
            .map(FurnitureMakeSlotItem::toProto)
            .toList();
    }
}
