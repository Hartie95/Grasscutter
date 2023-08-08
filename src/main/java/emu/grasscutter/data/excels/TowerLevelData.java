package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.tower.TowerCondType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ResourceType(name = "TowerLevelExcelConfigData.json")
@Data
@EqualsAndHashCode(callSuper = false)
public class TowerLevelData extends GameResource {

    @Getter(onMethod = @__(@Override))
    @SerializedName(value = "levelId")
    private int id;
    private int levelIndex;
    private int levelGroupId;
    private int dungeonId;
    private List<TowerChallengeCond> conds;
    private List<String> towerBuffConfigStrList;
    private int firstPassRewardId;
    private int monsterLevel;
    private List<Integer> firstMonsterList;
    private List<Integer> secondMonsterList;
    // not in excels
    private List<TowerBuffConfigLevelList> towerBuffConfigFloorList;

    @Override
    public void onLoad() {
        this.towerBuffConfigFloorList = this.towerBuffConfigStrList.stream()
            .map(TowerBuffConfigLevelList::new)
            .toList();
        this.conds.forEach(TowerChallengeCond::onLoad);
    }

    /**
     * towerCondType
     * case TOWER_COND_LEFT_HP_GREATER_THAN:
     * argumentList : [233741003, 3024, 40] = [groupId, configId, remaining health %]
     * */
    @Getter
    @ToString
    public static class TowerChallengeCond {
        TowerCondType towerCondType;
        @SerializedName(value = "argumentListFirstHalf", alternate = {"OECDJKGBNFE"})
        List<Integer> argumentListFirstHalf;
        @SerializedName(value = "argumentListSecondHalf", alternate = {"argumentList"})
        List<Integer> argumentListSecondHalf;

        // not in excels
        ParseArgs upperHalfCond;
        ParseArgs lowerHalfCond;
        public void onLoad() {
            this.upperHalfCond = new ParseArgs(this.towerCondType, this.argumentListFirstHalf);
            this.lowerHalfCond = new ParseArgs(this.towerCondType, this.argumentListSecondHalf);
        }

        @Getter
        @ToString
        public static class ParseArgs {
            TowerCondType towerCondType;
            int groupId;
            int configId;
            int targetHpPercentage;
            int targetLeftTime;

            public ParseArgs(TowerCondType cond, List<Integer> argumentList) {
                this.towerCondType = cond;
                if (cond == TowerCondType.TOWER_COND_LEFT_HP_GREATER_THAN) {
                    this.groupId = argumentList.get(0);
                    this.configId = argumentList.get(1);
                    this.targetHpPercentage = argumentList.get(2);
                } else {
                    this.targetLeftTime = argumentList.get(1);
                }
            }
        }
    }

    @Getter
    @ToString
    public static class TowerBuffConfigLevelList {
        final List<TowerBuffConfig> configList = new ArrayList<>();
        public TowerBuffConfigLevelList(String configStrList) {
            this.configList.addAll(Arrays.stream(configStrList.split(","))
                .map(TowerBuffConfig::new)
                .toList());
        }
    }

    @Getter
    @ToString
    public static class TowerBuffConfig {
        final int id;
        final int weight;

        public TowerBuffConfig (String configStr) {
            String[] splitStr = configStr.split(":");
            this.id = Integer.parseInt(splitStr[0]);
            this.weight = Integer.parseInt(splitStr[1]);
        }
    }
}
