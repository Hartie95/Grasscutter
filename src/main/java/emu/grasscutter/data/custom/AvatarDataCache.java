package emu.grasscutter.data.custom;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.AbilityEmbryoEntry;
import emu.grasscutter.data.binout.config.ConfigEntityAvatar;
import emu.grasscutter.data.excels.AvatarPromoteData;
import emu.grasscutter.data.excels.AvatarSkillDepotData;
import emu.grasscutter.utils.Language;
import emu.grasscutter.utils.Utils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.anime_game_servers.game_data_models.gi.data.entities.CreatureExcelConfig;
import org.anime_game_servers.game_data_models.gi.data.entities.avatar.AvatarCurveData;
import org.anime_game_servers.game_data_models.gi.data.entities.avatar.AvatarData;
import org.anime_game_servers.game_data_models.gi.data.entities.avatar.GrowthCurveType;
import org.anime_game_servers.game_data_models.gi.data.entities.avatar.WeaponType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class AvatarDataCache {
    // baseData
    private int avatarId;
    private AvatarData avatarData;
    private ConfigEntityAvatar configEntityAvatar;
    private String baseName;

    // abilities
    private Map<Integer, AvatarSkillDepotData> skillDepots;
    private int defaultSkillDepotId;
    private IntList abilities;
    private List<String> abilityNames = new ArrayList<>();

    // stat/prop growth
    private float[] hpGrowthCurve;
    private float[] attackGrowthCurve;
    private float[] defenseGrowthCurve;

    // promote data
    private Map<Integer, AvatarPromoteData> promoteData;

    // friendship level
    private List<Integer> fetters;
    private int nameCardRewardId;
    private int nameCardId;
    private static Pattern baseNamePattern = Pattern.compile("ConfigAvatar_(.+)");

    public AvatarDataCache(AvatarData data) {
        this.avatarId = data.getId();
        this.avatarData = data;

        val configDataName = GameData.getTextHashMap().get(data.getCombatConfigHashJvm());
        configEntityAvatar = GameData.getAvatarConfigData().get(configDataName);

        initBaseName(configDataName);

        initSkillDepotData();
        initFriendshipData();
        initAscensionData();
        initGrowthCurveData();
        rebuildAbilityEmbryo();
    }
    private void initBaseName(String configDataName){
        if(configDataName!=null) {
            val matcher = baseNamePattern.matcher(configDataName);
            if (matcher.find()) {
                this.baseName = matcher.group(1);
            } else {
                this.baseName = configDataName;
            }
        } else {
            this.baseName = Language.getTextMapKey(avatarData.getNameTextMapHash()).get(0);
        }
    }

    private void initSkillDepotData(){
        defaultSkillDepotId = avatarData.getSkillDepotId();
        var candDepotIds = avatarData.getCandSkillDepotIdsNonNull();
        if (candDepotIds.isEmpty()) {
            candDepotIds = List.of(defaultSkillDepotId);
        }
        this.skillDepots = candDepotIds.stream()
            .map(depotId -> GameData.getAvatarSkillDepotDataMap().get(depotId))
            .collect(Collectors.toMap(AvatarSkillDepotData::getId, depot -> depot));
    }

    private void initFriendshipData(){
        this.fetters = GameData.getFetterDataEntries().get(this.avatarId);

        if (GameData.getFetterCharacterCardDataMap().get(this.avatarId) != null) {
            this.nameCardRewardId = GameData.getFetterCharacterCardDataMap().get(this.avatarId).getRewardId();
        }

        if (GameData.getRewardDataMap().get(this.nameCardRewardId) != null) {
            this.nameCardId = GameData.getRewardDataMap().get(this.nameCardRewardId).getRewardItemList().get(0).getItemId();
        }
    }

    private void initAscensionData(){
        val promoteId = this.avatarData.getAvatarPromoteId();
        this.promoteData = GameData.getAvatarPromoteDataMap().values().stream()
            .filter(data -> data.getAvatarPromoteId() == promoteId)
            .collect(Collectors.toMap(AvatarPromoteData::getPromoteLevel, data -> data));
    }

    private void initGrowthCurveData(){
        int size = GameData.getAvatarCurveDataMap().size();
        this.hpGrowthCurve = new float[size];
        this.attackGrowthCurve = new float[size];
        this.defenseGrowthCurve = new float[size];
        for (val curveData : GameData.getAvatarCurveDataMap().values()) {
            int level = curveData.getLevel() - 1;
            for (CreatureExcelConfig.FightPropGrowConfig growCurve : this.avatarData.getPropGrowCurvesNonNull()) {
                val targetMap = switch (growCurve.getTypeNonNull()) {
                    case FIGHT_PROP_BASE_HP:
                        yield this.hpGrowthCurve;
                    case FIGHT_PROP_BASE_ATTACK:
                        yield this.attackGrowthCurve;
                    case FIGHT_PROP_BASE_DEFENSE:
                        yield this.defenseGrowthCurve;
                    default:
                        yield null;
                };
                if (targetMap == null) {
                    Grasscutter.getLogger().warn("Unknown prop: {}", growCurve.getType());
                    continue;
                }
                targetMap[level] = getGrowthCurveValue(curveData, growCurve.getGrowCurve());
            }
        }

        /*
        for (PropGrowCurve growCurve : this.PropGrowCurves) {
            FightProperty prop = FightProperty.getPropByName(growCurve.getType());
            this.growthCurveMap.put(prop.getId(), growCurve.getGrowCurve());
        }
        */
    }

    public void rebuildAbilityEmbryo() {
        // Cache abilities

        AbilityEmbryoEntry info = GameData.getAbilityEmbryo(this.baseName);
        if (info != null) {
            this.abilities = new IntArrayList(info.getAbilities().length);
            for (String ability : info.getAbilities()) {
                this.abilities.add(Utils.abilityHash(ability));
                abilityNames.add(ability);
            }
        }
    }

    public int getId(){
        return avatarId;
    }

    private float getGrowthCurveValue(AvatarCurveData curveData, GrowthCurveType growthCurveType) {
        val curveInfos = curveData.getCurveInfosNonNull();
        return curveInfos.stream()
            .filter(info -> growthCurveType.equals(info.getType()))
            .findFirst().map(AvatarCurveData.GrowCurveInfo::getValue).orElse(1f);
    }

    public float getBaseHp(int level) {
        val hpBase = avatarData.getHpBase();
        try {
            return hpBase * this.hpGrowthCurve[level - 1];
        } catch (Exception e) {
            return hpBase;
        }
    }

    public float getBaseAttack(int level) {
        val atkBase = avatarData.getAttackBase();
        try {
            return atkBase * this.attackGrowthCurve[level - 1];
        } catch (Exception e) {
            return atkBase;
        }
    }

    public float getBaseDefense(int level) {
        val defenseBase = avatarData.getAttackBase();
        try {
            return defenseBase * this.defenseGrowthCurve[level - 1];
        } catch (Exception e) {
            return defenseBase;
        }
    }

    public float getBaseCritical() {
        return avatarData.getCritical();
    }

    public float getBaseCriticalHurt() {
        return avatarData.getCriticalHurt();
    }

    public int getInitialWeapon(){
        return avatarData.getInitialWeapon();
    }

    @NonNull public WeaponType getWeaponType(){
        return avatarData.getWeaponTypeNonNull();
    }

}
