package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

import java.util.List;
import java.util.stream.IntStream;

@ResourceType(name = "TowerRewardExcelConfigData.json")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@SuppressWarnings("SpellCheckingInspection")
public class TowerRewardData extends GameResource {
    @SerializedName(value = "levelIndex", alternate = {"IEPBHNILIDB"})
    private int levelIndex;
    @SerializedName(value = "floorIndex", alternate = {"DGKHCGBPFDM"})
    private int floorIndex;
    @SerializedName(value = "threeStarsReward", alternate = {"BCOFGJBABGD"})
    private int threeStarsReward;
    @SerializedName(value = "sixStarsReward", alternate = {"CAEOHANFNAP"})
    private int sixStarsReward;
    @SerializedName(value = "nineStarsReward", alternate = {"DGJDIBJKGMI"})
    private int nineStarsReward;
    @SerializedName(value = "chamberBountyRewards", alternate = {"MHDLDDFOIJD"})
    private List<Integer> chamberBountyRewards;

    @Override
    public int getId() {
        return (this.levelIndex << 4) + this.floorIndex;
    }

    public List<Integer> getStarRewardsByStarCount(int starCount) {
        val fullStarRewards = List.of(this.threeStarsReward, this.sixStarsReward, this.nineStarsReward);
        return IntStream.rangeClosed(0, starCount).map(i -> (int) Math.floor((float) i / 3) - 1).filter(i -> i >= 0)
            .distinct().mapToObj(fullStarRewards::get).toList();
    }

    public List<Integer> getFirstPassRewardByStarCount(int starCount) {
        return IntStream.rangeClosed(0, starCount).map(i -> (int) Math.floor((float) i / 3) - 1).filter(i -> i >= 0)
            .distinct().mapToObj(this.chamberBountyRewards::get).toList();
    }
}
