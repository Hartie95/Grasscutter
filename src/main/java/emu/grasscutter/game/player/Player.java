package emu.grasscutter.game.player;

import dev.morphia.annotations.*;
import emu.grasscutter.GameConstants;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.*;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.Account;
import emu.grasscutter.game.CoopRequest;
import emu.grasscutter.game.ability.AbilityManager;
import emu.grasscutter.game.activity.ActivityManager;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.avatar.AvatarStorage;
import emu.grasscutter.game.avatar.TrialAvatar;
import emu.grasscutter.game.battlepass.BattlePassManager;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.expedition.ExpeditionInfo;
import emu.grasscutter.game.friends.FriendsList;
import emu.grasscutter.game.friends.PlayerProfile;
import emu.grasscutter.game.gacha.PlayerGachaInfo;
import emu.grasscutter.game.home.GameHome;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.Inventory;
import emu.grasscutter.game.mail.Mail;
import emu.grasscutter.game.mail.MailHandler;
import emu.grasscutter.game.managers.blossom.BlossomManager;
import emu.grasscutter.game.managers.cooking.ActiveCookCompoundData;
import emu.grasscutter.game.managers.cooking.CookingCompoundManager;
import emu.grasscutter.game.managers.cooking.CookingManager;
import emu.grasscutter.game.managers.FurnitureManager;
import emu.grasscutter.game.managers.ResinManager;
import emu.grasscutter.game.managers.SotSManager;
import emu.grasscutter.game.managers.deforestation.DeforestationManager;
import emu.grasscutter.game.managers.energy.EnergyManager;
import emu.grasscutter.game.managers.forging.ActiveForgeData;
import emu.grasscutter.game.managers.forging.ForgingManager;
import emu.grasscutter.game.managers.mapmark.MapMark;
import emu.grasscutter.game.managers.mapmark.MapMarksManager;
import emu.grasscutter.game.managers.stamina.StaminaManager;
import emu.grasscutter.game.props.*;
import emu.grasscutter.game.quest.QuestManager;
import emu.grasscutter.game.quest.enums.QuestCond;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.shop.ShopLimit;
import emu.grasscutter.game.tower.TowerData;
import emu.grasscutter.game.tower.TowerManager;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.game.world.World;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.proto.AbilityInvokeEntryOuterClass.AbilityInvokeEntry;
import emu.grasscutter.net.proto.AttackResultOuterClass.AttackResult;
import emu.grasscutter.net.proto.CombatInvokeEntryOuterClass.CombatInvokeEntry;
import emu.grasscutter.net.proto.GadgetInteractReqOuterClass.GadgetInteractReq;
import emu.grasscutter.net.proto.MpSettingTypeOuterClass.MpSettingType;
import emu.grasscutter.net.proto.OnlinePlayerInfoOuterClass.OnlinePlayerInfo;
import emu.grasscutter.net.proto.PlayerApplyEnterMpResultNotifyOuterClass;
import emu.grasscutter.net.proto.PlayerLocationInfoOuterClass.PlayerLocationInfo;
import emu.grasscutter.net.proto.PlayerWorldLocationInfoOuterClass.PlayerWorldLocationInfo;
import emu.grasscutter.net.proto.ProfilePictureOuterClass.ProfilePicture;
import emu.grasscutter.net.proto.PropChangeReasonOuterClass.PropChangeReason;
import emu.grasscutter.net.proto.ShowAvatarInfoOuterClass.ShowAvatarInfo;
import emu.grasscutter.net.proto.SocialDetailOuterClass.SocialDetail;
import emu.grasscutter.net.proto.SocialShowAvatarInfoOuterClass.SocialShowAvatarInfo;
import emu.grasscutter.net.proto.TrialAvatarGrantRecordOuterClass.TrialAvatarGrantRecord.GrantReason;
import emu.grasscutter.scripts.data.SceneRegion;
import emu.grasscutter.server.event.player.PlayerJoinEvent;
import emu.grasscutter.server.event.player.PlayerQuitEvent;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.game.GameSession.SessionState;
import emu.grasscutter.server.packet.send.*;
import emu.grasscutter.utils.DateHelper;
import emu.grasscutter.utils.MessageHandler;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static emu.grasscutter.config.Configuration.GAME_OPTIONS;

@Entity(value = "players", useDiscriminator = false)
public class Player {
    @Id private int id;
    @Indexed(options = @IndexOptions(unique = true)) private String accountId;
    @Setter private transient Account account;
    @Getter @Setter private transient GameSession session;

    @Getter private String nickname;
    @Getter private String signature;
    @Getter private int headImage;
    @Getter private int nameCardId = 210001;
    @Getter private final Position position;
    @Getter private final Position rotation;
    @Getter private PlayerBirthday birthday;
    @Getter private PlayerCodex codex;
    @Getter @Setter private boolean showAvatars;
    @Getter @Setter private List<Integer> showAvatarList;
    @Getter @Setter private List<Integer> showNameCardList;
    @Getter private final Map<Integer, Integer> properties;
    @Getter @Setter private int currentRealmId;
    @Getter @Setter private int widgetId;
    @Getter @Setter private int sceneId;
    @Getter @Setter private int regionId;
    @Getter private int mainCharacterId;
    @Setter private boolean godmode;  // Getter is inGodmode
    private boolean stamina;  // Getter is getUnlimitedStamina, Setter is setUnlimitedStamina

    @Getter private final Set<Integer> nameCardList;
    @Getter private final Set<Integer> flyCloakList;
    @Getter private final Set<Integer> costumeList;
    @Getter private final Set<Integer> personalLineList;
    @Getter @Setter private Set<Integer> rewardedLevels;
    @Getter @Setter private Set<Integer> realmList;
    @Getter private final Set<Integer> unlockedForgingBlueprints;
    @Getter private final Set<Integer> unlockedCombines;
    @Getter private final Set<Integer> unlockedFurniture;
    @Getter private final Set<Integer> unlockedFurnitureSuite;
    @Getter private final Map<Long, ExpeditionInfo> expeditionInfo;
    @Getter private final Map<Integer, Integer> unlockedRecipies;
    @Getter private final List<ActiveForgeData> activeForges;
    @Getter private final Map<Integer, ActiveCookCompoundData> activeCookCompounds;
    @Getter private final Map<Integer, Integer> questGlobalVariables;
    @Getter private final Map<Integer, Integer> openStates;
    @Getter @Setter private Map<Integer, Set<Integer>> unlockedSceneAreas;
    @Getter @Setter private Map<Integer, Set<Integer>> unlockedScenePoints;
    @Getter @Setter private List<Integer> chatEmojiIdList;

    @Transient private long nextGuid = 0;
    @Transient @Getter @Setter private int peerId;
    @Transient private World world;  // Synchronized getter and setter
    @Transient private Scene scene;  // Synchronized getter and setter
    @Transient @Getter private int weatherId = 0;
    @Transient @Getter private ClimateType climate = ClimateType.CLIMATE_SUNNY;

    // Player managers go here
    @Getter private transient final AvatarStorage avatars;
    @Getter private transient final Inventory inventory;
    @Getter private transient final FriendsList friendsList;
    @Getter private transient final MailHandler mailHandler;
    @Getter @Setter private transient MessageHandler messageHandler;
    @Getter private transient final AbilityManager abilityManager;
    @Getter @Setter private transient QuestManager questManager;
    @Getter private transient final TowerManager towerManager;
    @Getter private transient SotSManager sotsManager;
    @Getter private transient MapMarksManager mapMarksManager;
    @Getter private transient StaminaManager staminaManager;
    @Getter private transient EnergyManager energyManager;
    @Getter private transient ResinManager resinManager;
    @Getter private transient ForgingManager forgingManager;
    @Getter private transient DeforestationManager deforestationManager;
    @Getter private transient FurnitureManager furnitureManager;
    @Getter private transient BattlePassManager battlePassManager;
    @Getter private transient CookingManager cookingManager;
    @Getter private transient CookingCompoundManager cookingCompoundManager;
    @Getter private transient ActivityManager activityManager;
    @Getter private transient final PlayerBuffManager buffManager;
    @Getter private transient PlayerProgressManager progressManager;
    @Getter private transient final BlossomManager blossomManager;

    @Getter @Setter private transient Position lastCheckedPosition = null;

    // Manager data (Save-able to the database)
    private PlayerProfile playerProfile;  // Getter has null-check
    @Getter private TeamManager teamManager;
    private TowerData towerData;  // Getter has null-check
    @Getter private final PlayerGachaInfo gachaInfo;
    private PlayerCollectionRecords collectionRecordStore;  // Getter has null-check
    @Getter private final ArrayList<ShopLimit> shopLimit;

    @Getter private transient GameHome home;

    @Setter private boolean moonCard;  // Getter is inMoonCard
    @Getter @Setter private Date moonCardStartTime;
    @Getter @Setter private int moonCardDuration;
    @Getter @Setter private Set<Date> moonCardGetTimes;

    @Transient @Getter private boolean paused;
    @Transient @Getter @Setter private int enterSceneToken;
    @Transient @Getter @Setter private SceneLoadState sceneLoadState = SceneLoadState.NONE;
    @Transient private boolean hasSentLoginPackets;
    @Transient private long nextSendPlayerLocTime = 0;

    private transient final Int2ObjectMap<CoopRequest> coopRequests;  // Synchronized getter
    @Getter private transient final Queue<AttackResult> attackResults;
    @Getter private transient final InvokeHandler<CombatInvokeEntry> combatInvokeHandler;
    @Getter private transient final InvokeHandler<AbilityInvokeEntry> abilityInvokeHandler;
    @Getter private transient final InvokeHandler<AbilityInvokeEntry> clientAbilityInitFinishHandler;

    @Getter @Setter private long springLastUsed;
    private HashMap<String, MapMark> mapMarks;  // Getter makes an empty hashmap - maybe do this elsewhere?
    @Getter @Setter private int nextResinRefresh;
    @Getter @Setter private int resinBuyCount;
    @Getter @Setter private int lastDailyReset;
    @Getter private transient final MpSettingType mpSetting = MpSettingType.MP_SETTING_TYPE_ENTER_AFTER_APPLY;  // TODO
    @Getter private long playerGameTime = 540;
    @Getter private final PlayerProgress playerProgress;
    @Getter private final Set<Integer> activeQuestTimers;

    @Deprecated
    @SuppressWarnings({"rawtypes", "unchecked"}) // Morphia only!
    public Player() {
        this.inventory = new Inventory(this);
        this.avatars = new AvatarStorage(this);
        this.friendsList = new FriendsList(this);
        this.mailHandler = new MailHandler(this);
        this.towerManager = new TowerManager(this);
        this.abilityManager = new AbilityManager(this);
        this.deforestationManager = new DeforestationManager(this);
        this.questManager = new QuestManager(this);
        this.buffManager = new PlayerBuffManager(this);
        this.position = new Position(GameConstants.START_POSITION);
        this.rotation = new Position(0, 307, 0);
        this.sceneId = 3;
        this.regionId = 1;
        this.properties = new HashMap<>();
        for (PlayerProperty prop : PlayerProperty.values()) {
            if (prop.getId() < 10000) {
                continue;
            }
            this.properties.put(prop.getId(), 0);
        }

        this.gachaInfo = new PlayerGachaInfo();
        this.nameCardList = new HashSet<>();
        this.flyCloakList = new HashSet<>();
        this.costumeList = new HashSet<>();
        this.personalLineList = new HashSet<>();
        this.towerData = new TowerData();
        this.collectionRecordStore = new PlayerCollectionRecords();
        this.unlockedForgingBlueprints = new HashSet<>();
        this.unlockedCombines = new HashSet<>();
        this.unlockedFurniture = new HashSet<>();
        this.unlockedFurnitureSuite = new HashSet<>();
        this.activeCookCompounds=new HashMap<>();
        this.activeForges = new ArrayList<>();
        this.unlockedRecipies = new HashMap<>();
        this.questGlobalVariables = new HashMap<>();
        this.openStates = new HashMap<>();
        this.unlockedSceneAreas = new HashMap<>();
        this.unlockedScenePoints = new HashMap<>();
        this.chatEmojiIdList = new ArrayList<>();
        this.playerProgress = new PlayerProgress();
        this.activeQuestTimers = new HashSet<>();

        this.attackResults = new LinkedBlockingQueue<>();
        this.coopRequests = new Int2ObjectOpenHashMap<>();
        this.combatInvokeHandler = new InvokeHandler(PacketCombatInvocationsNotify.class);
        this.abilityInvokeHandler = new InvokeHandler(PacketAbilityInvocationsNotify.class);
        this.clientAbilityInitFinishHandler = new InvokeHandler(PacketClientAbilityInitFinishNotify.class);

        this.birthday = new PlayerBirthday();
        this.rewardedLevels = new HashSet<>();
        this.moonCardGetTimes = new HashSet<>();
        this.codex = new PlayerCodex(this);
        this.progressManager = new PlayerProgressManager(this);
        this.shopLimit = new ArrayList<>();
        this.expeditionInfo = new HashMap<>();
        this.messageHandler = null;
        this.mapMarksManager = new MapMarksManager(this);
        this.staminaManager = new StaminaManager(this);
        this.sotsManager = new SotSManager(this);
        this.energyManager = new EnergyManager(this);
        this.resinManager = new ResinManager(this);
        this.forgingManager = new ForgingManager(this);
        this.progressManager = new PlayerProgressManager(this);
        this.furnitureManager = new FurnitureManager(this);
        this.cookingManager = new CookingManager(this);
        this.cookingCompoundManager = new CookingCompoundManager(this);
        this.blossomManager = new BlossomManager(this);
    }

    // On player creation
    public Player(GameSession session) {
        this();
        this.account = session.getAccount();
        this.accountId = this.getAccount().getId();
        this.session = session;
        this.nickname = "Traveler";
        this.signature = "";
        this.teamManager = new TeamManager(this);
        this.birthday = new PlayerBirthday();
        this.codex = new PlayerCodex(this);
        this.setProperty(PlayerProperty.PROP_PLAYER_LEVEL, 1, false);
        this.setProperty(PlayerProperty.PROP_IS_SPRING_AUTO_USE, 1, false);
        this.setProperty(PlayerProperty.PROP_SPRING_AUTO_USE_PERCENT, 50, false);
        this.setProperty(PlayerProperty.PROP_IS_FLYABLE, 1, false);
        this.setProperty(PlayerProperty.PROP_IS_TRANSFERABLE, 1, false);
        this.setProperty(PlayerProperty.PROP_MAX_STAMINA, 24000, false);
        this.setProperty(PlayerProperty.PROP_CUR_PERSIST_STAMINA, 24000, false);
        this.setProperty(PlayerProperty.PROP_PLAYER_RESIN, 160, false);
        this.getFlyCloakList().add(140001);
        this.getNameCardList().add(210001);
        this.messageHandler = null;
        this.mapMarksManager = new MapMarksManager(this);
        this.staminaManager = new StaminaManager(this);
        this.sotsManager = new SotSManager(this);
        this.energyManager = new EnergyManager(this);
        this.resinManager = new ResinManager(this);
        this.deforestationManager = new DeforestationManager(this);
        this.forgingManager = new ForgingManager(this);
        this.progressManager = new PlayerProgressManager(this);
        this.furnitureManager = new FurnitureManager(this);
        this.cookingManager = new CookingManager(this);
        this.cookingCompoundManager=new CookingCompoundManager(this);
    }

    public void updatePlayerGameTime(long gameTime){
        if(getPlayerGameTime() == gameTime) return;

        this.playerGameTime = gameTime;
        save();
    }

    public int getUid() {
        return this.id;
    }

    public void setUid(int id) {
        this.id = id;
    }

    public long getNextGameGuid() {
        return ((long) getUid() << 32) + ++this.nextGuid;
    }

    public Account getAccount() {
        if (this.account == null)
            this.account = DatabaseHelper.getAccountById(this.accountId);
        return this.account;
    }

    public boolean isOnline() {
        return getSession() != null && getSession().isActive();
    }

    public GameServer getServer() {
        return getSession().getServer();
    }

    public synchronized World getWorld() {
        return this.world;
    }

    public synchronized void setWorld(World world) {
        this.world = world;
    }

    public synchronized Scene getScene() {
        return this.scene;
    }

    public synchronized void setScene(Scene scene) {
        this.scene = scene;
    }

    synchronized public void setClimate(ClimateType climate) {
        this.climate = climate;
        this.session.send(new PacketSceneAreaWeatherNotify(this));
    }

    synchronized public void setWeather(int weather) {
        this.setWeather(weather, ClimateType.CLIMATE_NONE);
    }

    synchronized public void setWeather(int weatherId, ClimateType climate) {
        // Lookup default climate for this weather
        if (climate == ClimateType.CLIMATE_NONE) {
            WeatherData w = GameData.getWeatherDataMap().get(weatherId);
            if (w != null) {
                climate = w.getDefaultClimate();
            }
        }
        this.weatherId = weatherId;
        this.climate = climate;
        this.session.send(new PacketSceneAreaWeatherNotify(this));
    }

    public void setNickname(String nickName) {
        this.nickname = nickName;
        this.updateProfile();
    }

    public void setHeadImage(int picture) {
        this.headImage = picture;
        this.updateProfile();
    }

    public void setSignature(String signature) {
        this.signature = signature;
        this.updateProfile();
    }

    public void addRealmList(int realmId) {
        this.realmList = Optional.ofNullable(this.realmList)
            .orElseGet(HashSet::new);

        getRealmList().add(realmId);
    }

    public int getExpeditionLimit() {
        final int CONST_VALUE_EXPEDITION_INIT_LIMIT = 2;  // TODO: pull from ConstValueExcelConfigData.json
        return (int) GameData.getPlayerLevelDataMap().values().stream()
            .filter(data -> getLevel() >= data.getLevel()
                && data.getExpeditionLimitAdd() == 1)
            .count() + CONST_VALUE_EXPEDITION_INIT_LIMIT;
    }

    public Set<Integer> getUnlockedSceneAreas(int sceneId) {
        return getUnlockedSceneAreas().computeIfAbsent(sceneId, i -> new CopyOnWriteArraySet<>());
    }

    public Set<Integer> getUnlockedScenePoints(int sceneId) {
        return getUnlockedScenePoints().computeIfAbsent(sceneId, i -> new CopyOnWriteArraySet<>());
    }

    public int getLevel() {
        return getProperty(PlayerProperty.PROP_PLAYER_LEVEL);
    }

    public boolean setLevel(int level) {
        if (getLevel() == level || !setProperty(PlayerProperty.PROP_PLAYER_LEVEL, level)) return false;

        // Update world level and profile.
        updateWorldLevel();
        updateProfile();

        // Handle open state unlocks from level-up.
        getProgressManager().tryUnlockOpenStates();
        getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_PLAYER_LEVEL_UP, level);
        getQuestManager().queueEvent(QuestCond.QUEST_COND_PLAYER_LEVEL_EQUAL_GREATER, level);
        return true;
    }

    public int getExp() {
        return getProperty(PlayerProperty.PROP_PLAYER_EXP);
    }

    public int getWorldLevel() {
        return getProperty(PlayerProperty.PROP_PLAYER_WORLD_LEVEL);
    }

    public boolean setWorldLevel(int level) {
        if (!setProperty(PlayerProperty.PROP_PLAYER_WORLD_LEVEL, level)) return false;

        // Don't update World's WL if we are in someone else's world
        if (getWorld().getHost() == this) getWorld().setWorldLevel(level);

        this.updateProfile();
        return true;
    }

    public int getForgePoints() {
        return getProperty(PlayerProperty.PROP_PLAYER_FORGE_POINT);
    }

    public boolean setForgePoints(int value) {
        return value == getForgePoints() || setProperty(PlayerProperty.PROP_PLAYER_FORGE_POINT, value);
    }

    public int getPrimogems() {
        return getProperty(PlayerProperty.PROP_PLAYER_HCOIN);
    }

    public boolean setPrimogems(int primogem) {
        return setProperty(PlayerProperty.PROP_PLAYER_HCOIN, primogem);
    }

    public int getMora() {
        return getProperty(PlayerProperty.PROP_PLAYER_SCOIN);
    }

    public boolean setMora(int mora) {
        return setProperty(PlayerProperty.PROP_PLAYER_SCOIN, mora);
    }

    public int getCrystals() {
        return getProperty(PlayerProperty.PROP_PLAYER_MCOIN);
    }

    public boolean setCrystals(int crystals) {
        return setProperty(PlayerProperty.PROP_PLAYER_MCOIN, crystals);
    }

    public int getHomeCoin() {
        return getProperty(PlayerProperty.PROP_PLAYER_HOME_COIN);
    }

    public boolean setHomeCoin(int coin) {
        return setProperty(PlayerProperty.PROP_PLAYER_HOME_COIN, coin);
    }

    private int getExpRequired(int level) {
        PlayerLevelData levelData = GameData.getPlayerLevelDataMap().get(level);
        return levelData != null ? levelData.getExp() : 0;
    }

    private float getExpModifier() {
        return GAME_OPTIONS.rates.adventureExp;
    }

    // Affected by exp rate
    public void earnExp(int exp) {
        addExpDirectly((int) (exp * getExpModifier()));
    }

    // Directly give player exp
    public void addExpDirectly(int gain) {
        int level = getLevel();
        int exp = getExp();
        int reqExp = getExpRequired(level);

        exp += gain;

        while (exp >= reqExp && reqExp > 0) {
            exp -= reqExp;
            level += 1;
            reqExp = getExpRequired(level);

            // Set level each time to allow level-up specific logic to run.
            setLevel(level);
        }

        // Set exp
        setProperty(PlayerProperty.PROP_PLAYER_EXP, exp);
    }

    private void updateWorldLevel() {
        int newWorldLevel = Math.max(0, Math.min(((getLevel() - 15) / 5), 8));

        if (newWorldLevel != getWorldLevel()) {
            setWorldLevel(newWorldLevel);
        }
    }

    private void updateProfile() {
        getProfile().syncWithCharacter(this);
    }

    public boolean isFirstLoginEnterScene() {
        return !hasSentLoginPackets();
    }

    public TowerData getTowerData() {
        if (this.towerData == null) {
            // because of mistake, null may be saved as storage at some machine, this can be removed in future
            this.towerData = new TowerData();
        }
        return this.towerData;
    }

    public void onEnterRegion(SceneRegion region) {
        String enterRegionName = "ENTER_REGION_"+ region.config_id;
        getQuestManager().forEachActiveQuest(quest -> {
            TriggerExcelConfigData triggerData = quest.getTriggerByName(enterRegionName);
            if (triggerData == null || triggerData.getGroupId() != region.getGroupId()) return;

            // If trigger hasn't been fired yet
            if (Boolean.TRUE.equals(quest.getTriggers().put(enterRegionName, true))) return;

            //getSession().send(new PacketServerCondMeetQuestListUpdateNotify());
            getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_TRIGGER_FIRE, triggerData.getId(), 0);
        });
    }

    public void onLeaveRegion(SceneRegion region) {
        String leaveRegionName = "LEAVE_REGION_"+ region.config_id;
        getQuestManager().forEachActiveQuest(quest -> {
            TriggerExcelConfigData triggerData = quest.getTriggerByName(leaveRegionName);
            if (triggerData == null || triggerData.getGroupId() != region.getGroupId()) return;

            // If trigger hasn't been fired yet
            if (Boolean.TRUE.equals(quest.getTriggers().put(leaveRegionName, true))) return;

            getSession().send(new PacketServerCondMeetQuestListUpdateNotify());
            getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_TRIGGER_FIRE, triggerData.getId(),0);
        });
    }

    public PlayerProfile getProfile() {
        if (this.playerProfile == null) {
            this.playerProfile = new PlayerProfile(this);
        }
        return this.playerProfile;
    }

    public boolean setProperty(PlayerProperty prop, boolean value) {
        return setPropertyWithSanityCheck(prop, value ? 1 : 0, true);
    }
    public boolean setProperty(PlayerProperty prop, int value) {
        return setPropertyWithSanityCheck(prop, value, true);
    }

    public boolean setProperty(PlayerProperty prop, int value, boolean sendPacket) {
        return setPropertyWithSanityCheck(prop, value, sendPacket);
    }

    public int getProperty(PlayerProperty prop) {
        return getProperties().get(prop.getId());
    }
    public boolean getBoolProperty(PlayerProperty prop) {
        return getProperties().get(prop.getId()) == 1;
    }

    public synchronized Int2ObjectMap<CoopRequest> getCoopRequests() {
        return this.coopRequests;
    }

    public void setNameCardId(int nameCardId) {
        this.nameCardId = nameCardId;
        this.updateProfile();
    }

    public void setMainCharacterId(int mainCharacterId) {
        if (this.mainCharacterId == 0) {
            this.mainCharacterId = mainCharacterId;
        }
    }

    public int getClientTime() {
        return getSession().getClientTime();
    }

    public long getLastPingTime() {
        return getSession().getLastPingTime();
    }

    public void setPaused(boolean newPauseState) {
        boolean oldPauseState = isPaused();
        this.paused = newPauseState;

        if (newPauseState && !oldPauseState) {
            onPause();
        } else if (oldPauseState && !newPauseState) {
            onUnpause();
        }
    }

    public boolean isInMultiplayer() {
        return getWorld() != null && getWorld().isMultiplayer();
    }

    public boolean inMoonCard() {
        return this.moonCard;
    }

    public void addMoonCardDays(int days) {
        this.moonCardDuration += days;
    }

    public int getMoonCardRemainDays() {
        Calendar remainCalendar = Calendar.getInstance();
        remainCalendar.setTime(getMoonCardStartTime());
        remainCalendar.add(Calendar.DATE, getMoonCardDuration());

        return (int) ((remainCalendar.getTime().getTime() -
            DateHelper.onlyYearMonthDay(new Date()).getTime()) / (24 * 60 * 60 * 1000)); // By copilot
    }

    public boolean rechargeMoonCard() {
        if (getMoonCardDuration() > 150) return false;  // Can only stack up to 180 days

        getInventory().addItem(new GameItem(203, 300));
        if (!inMoonCard()) {
            setMoonCard(true);
            setMoonCardStartTime(DateHelper.onlyYearMonthDay(new Date()));
            setMoonCardDuration(30);
        } else {
            addMoonCardDays(30);
        }
        getMoonCardGetTimes().add(getMoonCardStartTime());
        return true;
    }

    public void getTodayMoonCard() {
        if (!inMoonCard()) {
            return;
        }
        Date now = DateHelper.onlyYearMonthDay(new Date());
        if (getMoonCardGetTimes().contains(now)) {
            return;
        }
        Calendar stopCalendar = Calendar.getInstance();
        stopCalendar.setTime(new Date());
        stopCalendar.add(Calendar.DATE, getMoonCardDuration());
        if (now.after(stopCalendar.getTime())) {
            setMoonCard(false);
            return;
        }
        getMoonCardGetTimes().add(now);
        setMoonCardDuration(getMoonCardDuration() - 1);
        getInventory().addItem(new GameItem(201, 90), ActionReason.BlessingRedeemReward);
        getSession().send(new PacketCardProductRewardNotify(getMoonCardRemainDays()));
    }

    public void addExpeditionInfo(long avatarGuid, int expId, int hourTime, int startTime) {
        ExpeditionInfo exp = new ExpeditionInfo();
        exp.setExpId(expId);
        exp.setHourTime(hourTime);
        exp.setState(1);
        exp.setStartTime(startTime);
        getExpeditionInfo().put(avatarGuid, exp);
    }

    public void removeExpeditionInfo(long avatarGuid) {
        getExpeditionInfo().remove(avatarGuid);
    }

    public ExpeditionInfo getExpeditionInfo(long avatarGuid) {
        return getExpeditionInfo().get(avatarGuid);
    }

    public ShopLimit getGoodsLimit(int goodsId) {
        return getShopLimit().stream().filter(x -> x.getShopGoodId() == goodsId).findFirst().orElse(null);
    }

    public void addShopLimit(int goodsId, int boughtCount, int nextRefreshTime) {
        ShopLimit target = getGoodsLimit(goodsId);
        if (target != null) {
            target.setHasBought(target.getHasBought() + boughtCount);
            target.setHasBoughtInPeriod(target.getHasBoughtInPeriod() + boughtCount);
            target.setNextRefreshTime(nextRefreshTime);
        } else {
            ShopLimit sl = new ShopLimit();
            sl.setShopGoodId(goodsId);
            sl.setHasBought(boughtCount);
            sl.setHasBoughtInPeriod(boughtCount);
            sl.setNextRefreshTime(nextRefreshTime);
            getShopLimit().add(sl);
        }
        save();
    }

    public boolean getUnlimitedStamina() {
        return this.stamina;
    }

    public void setUnlimitedStamina(boolean stamina) {
        this.stamina = stamina;
    }

    public boolean inGodmode() {
        return this.godmode;
    }

    public boolean hasSentLoginPackets() {
        return this.hasSentLoginPackets;
    }

    public void addAvatar(Avatar avatar, boolean addToCurrentTeam) {
        boolean result = getAvatars().addAvatar(avatar);

        if (result) {
            // Add starting weapon
            getAvatars().addStartingWeapon(avatar);
            // Done
            if (!hasSentLoginPackets()) return;

            // Recalc stats
            avatar.recalcStats();
            // Packet, show notice on left if the avatar will be added to the team
            sendPacket(new PacketAvatarAddNotify(avatar, addToCurrentTeam && getTeamManager().canAddAvatarToCurrentTeam()));
            if (!addToCurrentTeam) return;
            // If space in team, add
            getTeamManager().addAvatarToCurrentTeam(avatar);
        } else {
            // Failed adding avatar
        }
    }

    public void addAvatar(Avatar avatar) {
        addAvatar(avatar, true);
    }

    public void addAvatar(int avatarId) {
        // I don't see why we cant do this lol
        addAvatar(new Avatar(avatarId), true);
    }

    public boolean addTrialAvatar(int trialAvatarId, GrantReason reason, int questMainId){
        List<Integer> trialAvatarBasicParam = TrialAvatar.getTrialAvatarParam(trialAvatarId);
        if (trialAvatarBasicParam.isEmpty()) return false;

        TrialAvatar trialAvatar = new TrialAvatar(trialAvatarBasicParam, trialAvatarId, reason, questMainId);
        if (trialAvatar.getAvatarData() == null || !hasSentLoginPackets()) return false;

        // add trial avatar to storage
        boolean result = this.getAvatars().addAvatar(trialAvatar);
        if (!result) return false;

        trialAvatar.equipTrialItems();
        // Recalc stats
        trialAvatar.recalcStats();

        // Packet, mimic official server behaviour, add to player's bag but not saving to db
        sendPacket(new PacketAvatarAddNotify(trialAvatar, false));
        // add to avatar to temporary trial team
        getTeamManager().addAvatarToTrialTeam(trialAvatar);
        return true;
    }

    public boolean addTrialAvatarForQuest(int trialAvatarId, int questMainId) {
        getTeamManager().setupTrialAvatarTeamForQuest();
        if (!addTrialAvatar(
            trialAvatarId,
            GrantReason.GRANT_REASON_BY_QUEST,
            questMainId)) return false;
        // Packet, mimic official server behaviour, necessary to stop player from modifying team
        sendPacket(new PacketAvatarTeamUpdateNotify(this));

        getTeamManager().updateTeamEntities(false);
        return true;
    }

    public void addTrialAvatarsForDungeon(@NotNull List<Integer> trialAvatarIds, GrantReason reason) {
        getTeamManager().setupTrialAvatarTeamForDungeon();
        trialAvatarIds.forEach(trialAvatarId -> addTrialAvatar(trialAvatarId, reason, 0));
    }

    public boolean removeTrialAvatarForQuest(int trialAvatarId) {
        if (!getTeamManager().isUseTrialTeam()) return false;

        List<Integer> trialAvatarBasicParam = TrialAvatar.getTrialAvatarParam(trialAvatarId);
        if (trialAvatarBasicParam.isEmpty()) return false;

        // allows player to modify team again
        sendPacket(new PacketAvatarTeamUpdateNotify());

        long trialAvatarGuid = getTeamManager().getEntityGuids().get(trialAvatarBasicParam.get(0));

        // remove trial avatar from storage
        this.getAvatars().removeAvatarByGuid(trialAvatarGuid);

        // remove trial avatar entities
        getTeamManager().removeTrialAvatarTeam();

        // send remove packets
        sendPacket(new PacketAvatarDelNotify(List.of(trialAvatarGuid)));
        return true;
    }

    public void removeTrialAvatarForDungeon() {
        if (!getTeamManager().isUseTrialTeam()) return;
        List<Long> tempGuids = getTeamManager().getEntityGuids().values().stream().toList();

        // remove trial avatar from storage
        tempGuids.forEach(guid -> this.getAvatars().removeAvatarByGuid(guid));

        // remove trial avatar entities
        getTeamManager().removeTrialAvatarTeam();

        // send remove packets
        sendPacket(new PacketAvatarDelNotify(tempGuids));
    }

    public void addFlycloak(int flycloakId) {
        getFlyCloakList().add(flycloakId);
        sendPacket(new PacketAvatarGainFlycloakNotify(flycloakId));
    }

    public void addCostume(int costumeId) {
        getCostumeList().add(costumeId);
        sendPacket(new PacketAvatarGainCostumeNotify(costumeId));
    }

    public void addPersonalLine(int personalLineId) {
        getPersonalLineList().add(personalLineId);
        getSession().getPlayer().getQuestManager().queueEvent(QuestCond.QUEST_COND_PERSONAL_LINE_UNLOCK, personalLineId);
    }

    public void addNameCard(int nameCardId) {
        getNameCardList().add(nameCardId);
        sendPacket(new PacketUnlockNameCardNotify(nameCardId));
    }

    public void setNameCard(int nameCardId) {
        if (!getNameCardList().contains(nameCardId)) return;

        setNameCardId(nameCardId);
        sendPacket(new PacketSetNameCardRsp(nameCardId));
    }

    public void dropMessage(Object message) {
        Optional.ofNullable(getMessageHandler())
            .ifPresentOrElse(handler -> handler.append(message.toString()),
                () -> getServer().getChatSystem().sendPrivateMessageFromServer(getUid(), message.toString()));
    }

    /**
     * Overrides player's ability with scene's special ability.
     * Also replace active team with limited character if any.
     * For example Dvalin fight is limited to only main character and
     * has special missile ability.
     *
     * @param scene Current scene.
     */
    public void setAvatarsAbilityForScene(Scene scene){
        SceneData sData = scene.getSceneData();
        if (sData == null) return;

        Optional.ofNullable(GameData.getConfigLevelEntityDataMap().get(sData.getLevelEntityConfig()))
            .ifPresent(config -> {
                List<EntityAvatar> activeTeam = getTeamManager().getActiveTeam();

                // so far only main character is specified, might be a problem if scene specific other character
                List<EntityAvatar> specifiedAvatarList = Optional.ofNullable(sData.getSpecifiedAvatarList())
                    .orElseGet(ArrayList::new).stream()
                    .filter(id -> id == getMainCharacterId())
                    .map(id -> getAvatars().getAvatarById(id))
                    .filter(Objects::nonNull)
                    .map(avatar -> new EntityAvatar(scene, avatar))
                    .toList();

                if (!specifiedAvatarList.isEmpty()) {
                    // clear active team and add only the specific character
                    activeTeam.clear();
                    activeTeam.addAll(specifiedAvatarList);
                }

                // rebuild active team with special abilities
                activeTeam.stream()
                    .map(EntityAvatar::getAvatar)
                    .map(Avatar::getAvatarData)
                    .filter(Objects::nonNull)
                    .forEach(avatarData -> {
                        avatarData.rebuildAbilityEmbryo();

                        Optional.ofNullable(config.getAvatarAbilities())
                            .orElseGet(ArrayList::new)
                            .forEach(abilities -> avatarData.getAbilities()
                                .add(Utils.abilityHash(abilities.getAbilityName())));
                    });
            });
    }
    /**
     * Sends a message to another player.
     *
     * @param sender  The sender of the message.
     * @param message The message to send.
     */
    public void sendMessage(Player sender, Object message) {
        getServer().getChatSystem().sendPrivateMessage(sender, getUid(), message.toString());
    }

    // ---------------------MAIL------------------------

    public List<Mail> getAllMail() { return getMailHandler().getMail(); }

    public void sendMail(Mail message) {
        getMailHandler().sendMail(message);
    }

    public boolean deleteMail(int mailId) {
        return getMailHandler().deleteMail(mailId);
    }

    public Mail getMail(int index) { return getMailHandler().getMailById(index); }

    public int getMailId(Mail message) {
        return getMailHandler().getMailIndex(message);
    }

    public boolean replaceMailByIndex(int index, Mail message) {
        return getMailHandler().replaceMailByIndex(index, message);
    }

    public void interactWith(int gadgetEntityId, GadgetInteractReq interactReq) {
        Optional.ofNullable(getScene().getEntityById(gadgetEntityId))
            .ifPresent(target -> target.onInteract(this, interactReq));
    }

    public void onPause() {
        getStaminaManager().stopSustainedStaminaHandler();
    }

    public void onUnpause() {
        getStaminaManager().startSustainedStaminaHandler();
    }

    public void sendPacket(BasePacket packet) {
        getSession().send(packet);
    }

    public OnlinePlayerInfo getOnlinePlayerInfo() {
        return OnlinePlayerInfo.newBuilder()
            .setUid(getUid())
            .setNickname(getNickname())
            .setPlayerLevel(getLevel())
            .setMpSettingType(getMpSetting())
            .setNameCardId(getNameCardId())
            .setSignature(getSignature())
            .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(getHeadImage()))
            .setCurPlayerNumInWorld(getWorld() != null ? getWorld().getPlayerCount() : 1)
            .build();
    }

    public void setBirthday(int d, int m) {
        this.birthday = new PlayerBirthday(d, m);
        this.updateProfile();
    }

    public boolean hasBirthday() {
        return getBirthday().getDay() > 0;
    }

    public List<SocialShowAvatarInfo> socialShowAvatarListProto(List<Integer> avatarIds) {
        return avatarIds == null ? List.of() : avatarIds.stream()
            .map(avatarId -> SocialShowAvatarInfo.newBuilder()
                .setAvatarId(avatarId)
                .setLevel(getAvatars().getAvatarById(avatarId).getLevel())
                .setCostumeId(getAvatars().getAvatarById(avatarId).getCostume())
                .build())
            .toList();
    }

    public SocialDetail.Builder getSocialDetail() {
        List<Integer> showAvatarList = isOnline() ? getShowAvatarList() :
            DatabaseHelper.getPlayerByUid(getUid()).getShowAvatarList();

        if (!isOnline()) {
            AvatarStorage avatars = DatabaseHelper.getPlayerByUid(getUid()).getAvatars();
            avatars.loadFromDatabase();
        }

        return SocialDetail.newBuilder()
            .setUid(getUid())
            .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(getHeadImage()))
            .setNickname(getNickname())
            .setSignature(getSignature())
            .setLevel(getLevel())
            .setBirthday(getBirthday().getFilledProtoWhenNotEmpty())
            .setWorldLevel(getWorldLevel())
            .setNameCardId(getNameCardId())
            .setIsShowAvatar(isShowAvatars())
            .addAllShowAvatarInfoList(socialShowAvatarListProto(showAvatarList))
            .addAllShowNameCardIdList(getShowNameCardInfoList())
            .setFinishAchievementNum(0)
            .setFriendEnterHomeOptionValue(getHome() == null ? 0 : getHome().getEnterHomeOption());
    }

    public List<ShowAvatarInfo> getShowAvatarInfoList() {
        Player player = isOnline() ? this : DatabaseHelper.getPlayerByUid(getUid());
        if (!isOnline()) {
            player.getAvatars().loadFromDatabase();
            player.getInventory().loadFromDatabase();
        }

        List<Avatar> showAvatarList = Optional.ofNullable(player.getShowAvatarList())
            .orElseGet(ArrayList::new).stream()
            .map(avatarId -> player.getAvatars().getAvatarById(avatarId))
            .toList();

        if (!isOnline()) {
            showAvatarList.forEach(Avatar::recalcStats);
        }

        return showAvatarList.stream().map(Avatar::toShowAvatarInfoProto).toList();
    }

    public List<Integer> getShowNameCardInfoList() {
        return Optional.ofNullable(getShowNameCardList())
            .orElseGet(ArrayList::new);
    }

    public PlayerWorldLocationInfo getWorldPlayerLocationInfo() {
        return PlayerWorldLocationInfo.newBuilder()
                .setSceneId(getSceneId())
                .setPlayerLoc(getPlayerLocationInfo())
                .build();
    }

    public PlayerLocationInfo getPlayerLocationInfo() {
        return PlayerLocationInfo.newBuilder()
                .setUid(getUid())
                .setPos(getPosition().toProto())
                .setRot(getRotation().toProto())
                .build();
    }

    public void loadBattlePassManager() {
        if (getBattlePassManager() == null) {
            this.battlePassManager = DatabaseHelper.loadBattlePass(this);
            getBattlePassManager().getMissions().values().removeIf(mission -> mission.getData() == null);
        }
    }

    public PlayerCollectionRecords getCollectionRecordStore() {
        if (this.collectionRecordStore == null) {
            this.collectionRecordStore = new PlayerCollectionRecords();
        }
        return this.collectionRecordStore;
    }

    public Map<String, MapMark> getMapMarks() {
        if (this.mapMarks == null) {
            this.mapMarks = new HashMap<>();
        }
        return mapMarks;
    }

    private boolean expireCoopRequest(CoopRequest req) {
        if (!req.isExpired()) return false;
        req.getRequester().sendPacket(new PacketPlayerApplyEnterMpResultNotify(
            this,
            false,
            PlayerApplyEnterMpResultNotifyOuterClass.PlayerApplyEnterMpResultNotify.Reason.REASON_SYSTEM_JUDGE));
        return true;
    }

    public synchronized void onTick() {
        // Check ping
        if (getLastPingTime() > System.currentTimeMillis() + 60000) {
            getSession().close();
            return;
        }
        // Check co-op requests
        getCoopRequests().values().removeIf(this::expireCoopRequest);
        // Handle buff
        getBuffManager().onTick();
        // Ping
        if (getWorld() != null) {
            // RTT notify - very important to send this often
            sendPacket(new PacketWorldPlayerRTTNotify(getWorld()));

            // Update player locations if in multiplayer every 5 seconds
            long time = System.currentTimeMillis();
            if (getWorld().isMultiplayer() && getScene() != null && time > this.nextSendPlayerLocTime) {
                sendPacket(new PacketWorldPlayerLocationNotify(getWorld()));
                sendPacket(new PacketScenePlayerLocationNotify(getScene()));
                resetSendPlayerLocTime();
            }
        }

        // Handle daily reset.
        doDailyReset();

        // Expedition
        int timeNow = Utils.getCurrentSeconds();
        AtomicBoolean needNotify = new AtomicBoolean(false);

        getExpeditionInfo().values().stream()
            .filter(e -> e.getState() == 1 && (timeNow - e.getStartTime() >= e.getHourTime() * 60 * 60))
            .forEach(e -> {
                e.setState(2);
                needNotify.set(true);
            });

        if (needNotify.get()) {
            save();
            sendPacket(new PacketAvatarExpeditionDataNotify(getExpeditionInfo()));
        }

        // Send updated forge queue data, if necessary.
        getForgingManager().sendPlayerForgingUpdate();

        // Recharge resin.
        getResinManager().rechargeResin();

        // Quest tick handling
        getQuestManager().onTick();
    }

    private synchronized void doDailyReset() {
        // Check if we should execute a daily reset on this tick.
        int currentTime = Utils.getCurrentSeconds();

        LocalDate currentDate = LocalDate.ofInstant(Instant.ofEpochSecond(currentTime), ZoneId.systemDefault());
        LocalDate lastResetDate = LocalDate.ofInstant(Instant.ofEpochSecond(getLastDailyReset()), ZoneId.systemDefault());

        if (!currentDate.isAfter(lastResetDate)) return;

        // We should - now execute all the resetting logic we need.
        // Reset forge points.
        setForgePoints(300_000);

        // Reset daily BP missions.
        getBattlePassManager().resetDailyMissions();

        // Trigger login BP mission, so players who are online during the reset
        // don't have to relog to clear the mission.
        getBattlePassManager().triggerMission(WatcherTriggerType.TRIGGER_LOGIN);

        // Reset weekly BP missions.
        if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            getBattlePassManager().resetWeeklyMissions();
        }
        // Reset resin-buying count.
        setResinBuyCount(0);

        // Done. Update last reset time.
        setLastDailyReset(currentTime);
    }

    public void resetSendPlayerLocTime() {
        this.nextSendPlayerLocTime = System.currentTimeMillis() + 5000;
    }

    @PostLoad
    private void onLoad() {
        getCodex().setPlayer(this);
        getProgressManager().setPlayer(this);
        getTeamManager().setPlayer(this);
    }

    public void save() {
        DatabaseHelper.savePlayer(this);
    }

    // Called from tokenrsp
    public void loadFromDatabase() {
        // Make sure these exist
        if (getTeamManager() == null) {
            this.teamManager = new TeamManager(this);
        }
        if (getCodex() == null) {
            this.codex = new PlayerCodex(this);
        }
        if (getProfile().getUid() == 0) {
            getProfile().syncWithCharacter(this);
        }

        // Load from db
        getAvatars().loadFromDatabase();
        getInventory().loadFromDatabase();

        getFriendsList().loadFromDatabase();
        getMailHandler().loadFromDatabase();
        getQuestManager().loadFromDatabase();

        loadBattlePassManager();
        getAvatars().postLoad(); // Needs to be called after inventory is handled
    }

    public void onPlayerBorn() {
        getQuestManager().onPlayerBorn();
    }

    public void onLogin() {
        // Quest - Commented out because a problem is caused if you log out while this quest is active
        /*
        if (getQuestManager().getMainQuestById(351) == null) {
            GameQuest quest = getQuestManager().addQuest(35104);
            if (quest != null) {
                quest.finish();
            }
            getQuestManager().addQuest(35101);

            this.setSceneId(3);
            this.getPos().set(GameConstants.START_POSITION);
        }
        */

        // Create world
        World world = new World(this);
        world.addPlayer(this);

        // Multiplayer setting
        setProperty(PlayerProperty.PROP_PLAYER_MP_SETTING_TYPE, getMpSetting().getNumber(), false);
        setProperty(PlayerProperty.PROP_IS_MP_MODE_AVAILABLE, 1, false);

        // Execute daily reset logic if this is a new day.
        doDailyReset();

        // Activity needed for some quests
        this.activityManager = new ActivityManager(this);

        // Rewind active quests, and put the player to a rewind position it finds (if any) of an active quest
        getQuestManager().onLogin();

        // Packets
        getSession().send(new PacketPlayerDataNotify(this)); // Player data
        getSession().send(new PacketStoreWeightLimitNotify());
        getSession().send(new PacketPlayerStoreNotify(this));
        getSession().send(new PacketAvatarDataNotify(this));
        getProgressManager().onPlayerLogin();

        getSession().send(new PacketFinishedParentQuestNotify(this));
        getBlossomManager().onPlayerLogin(); // this is the real order in official

        getSession().send(new PacketBattlePassAllDataNotify(this));
        getSession().send(new PacketQuestListNotify(this));
        getSession().send(new PacketCodexDataFullNotify(this));
        getSession().send(new PacketAllWidgetDataNotify(this));
        getSession().send(new PacketWidgetGadgetAllDataNotify());
        getSession().send(new PacketCombineDataNotify(getUnlockedCombines()));
        getSession().send(new PacketGetChatEmojiCollectionRsp(getChatEmojiIdList()));

        getForgingManager().sendForgeDataNotify();
        getResinManager().onPlayerLogin();
        getCookingManager().sendCookDataNotify();
        getCookingCompoundManager().onPlayerLogin();
        getTeamManager().onPlayerLogin();

        getTodayMoonCard(); // The timer works at 0:0, some users log in after that, use this method to check if they have received a reward today or not. If not, send the reward.

        // Battle Pass trigger
        getBattlePassManager().triggerMission(WatcherTriggerType.TRIGGER_LOGIN);

        getFurnitureManager().onLogin();
        // Home
        this.home = GameHome.getByUid(getUid());
        getHome().onOwnerLogin(this);

        getSession().send(new PacketPlayerEnterSceneNotify(this)); // Enter game world
        getSession().send(new PacketPlayerLevelRewardUpdateNotify(getRewardedLevels()));

        // First notify packets sent
        this.hasSentLoginPackets = true;

        // Set session state
        getSession().setState(SessionState.ACTIVE);

        // Call join event.
        PlayerJoinEvent event = new PlayerJoinEvent(this); event.call();
        if (event.isCanceled()) { // If event is not cancelled, continue.
            getSession().close();
            return;
        }

        // register
        getServer().registerPlayer(this);
        getProfile().setPlayer(this); // Set online
    }

    public void onLogout() {
        try {
            // Clear chat history.
            getServer().getChatSystem().clearHistoryOnLogout(this);

            // stop stamina calculation
            getStaminaManager().stopSustainedStaminaHandler();

            // force to leave the dungeon (inside has an "if")
            getServer().getDungeonSystem().exitDungeon(this);

            // Leave world
            if (getWorld() != null) {
                getWorld().removePlayer(this);
            }

            // Status stuff
            getProfile().syncWithCharacter(this);
            getProfile().setPlayer(null); // Set offline

            getCoopRequests().clear();

            // Save to db
            save();
            getTeamManager().saveAvatars();
            getFriendsList().save();

            // Call quit event.
            PlayerQuitEvent event = new PlayerQuitEvent(this); event.call();
        } catch (Throwable e) {
            e.printStackTrace();
            Grasscutter.getLogger().warn("Player (UID {}) save failure", getUid());
        } finally {
            removeFromServer();
        }
    }

    public void removeFromServer() {
        // Remove from server.
        //Note: DON'T DELETE BY UID,BECAUSE THERE ARE MULTIPLE SAME UID PLAYERS WHEN DUPLICATED LOGIN!
        //so I decide to delete by object rather than uid
        getServer().getPlayers().values().removeIf(player1 -> player1 == this);
    }

    public int getLegendaryKey() {
        return getProperty(PlayerProperty.PROP_PLAYER_LEGENDARY_KEY);
    }
    public synchronized void addLegendaryKey(int count) {
        setProperty(PlayerProperty.PROP_PLAYER_LEGENDARY_KEY, getLegendaryKey() + count);
    }
    public synchronized void useLegendaryKey(int count) {
        setProperty(PlayerProperty.PROP_PLAYER_LEGENDARY_KEY, getLegendaryKey() - count);
    }

    public enum SceneLoadState {
        NONE(0), LOADING(1), INIT(2), LOADED(3);

        @Getter private final int value;

        SceneLoadState(int value) {
            this.value = value;
        }
    }

    public int getPropertyMin(PlayerProperty prop) {
        return prop.isDynamicRange() ? 0 : prop.getMin();
    }

    public int getPropertyMax(PlayerProperty prop) {
        if (!prop.isDynamicRange()) return prop.getMax();

        return switch (prop) {
            case PROP_CUR_SPRING_VOLUME -> getProperty(PlayerProperty.PROP_MAX_SPRING_VOLUME);
            case PROP_CUR_PERSIST_STAMINA -> getProperty(PlayerProperty.PROP_MAX_STAMINA);
            default -> 0;
        };
    }

    public boolean isValueInPropBounds(PlayerProperty prop, int value){
        return getPropertyMin(prop) <= value && value <= getPropertyMax(prop);
    }

    private boolean setPropertyWithSanityCheck(PlayerProperty prop, int value, boolean sendPacket) {
        if(!isValueInPropBounds(prop, value)) return false;

        int currentValue = getProperties().get(prop.getId());
        getProperties().put(prop.getId(), value);
        if (sendPacket) {
            // Update player with packet
            sendPacket(new PacketPlayerPropNotify(this, prop));
            sendPacket(new PacketPlayerPropChangeNotify(this, prop, value - currentValue));

            // Make the Adventure EXP pop-up show on screen.
            if (prop == PlayerProperty.PROP_PLAYER_EXP) {
                sendPacket(new PacketPlayerPropChangeReasonNotify(
                    this, prop, currentValue, value,
                    PropChangeReason.PROP_CHANGE_REASON_PLAYER_ADD_EXP));
            }
        }
        return true;
    }
}
