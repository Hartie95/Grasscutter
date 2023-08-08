package emu.grasscutter.data.excels;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.dungeons.enums.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@ResourceType(name = "DungeonExcelConfigData.json")
@ToString
@Data
public class DungeonData extends GameResource {

    @Getter(onMethod = @__(@Override))
	private int id;
    private int serialId;
	private int sceneId;
	private int showLevel;
    private int levelRevise;
    private DungeonType type;
    private DungeonSubType subType;
    private DungeonPlayType playType;
    private DungeonInvolveType involveType;
	private int limitLevel;
	private int passCond;
	private int reviveMaxCount;
	private int settleCountdownTime;
	private int failSettleCountdownTime;
	private int quitSettleCountdownTime;
	private List<SettleShowType> settleShows;
    @SerializedName(value = "passRewardPreviewID", alternate = {"passRewardPreviewId"})
	private int passRewardPreviewId;
    private int statueCostID;
    private int statueCostCount;
    private String stateType;

    // not part of DungeonExcelConfigData
    private RewardPreviewData rewardPreviewData;

    public DungeonType getType() {
        return Optional.ofNullable(this.type).orElse(DungeonType.DUNGEON_NONE);
    }

    public DungeonSubType getSubType() {
        return Optional.ofNullable(this.subType).orElse(DungeonSubType.DUNGEON_SUB_NONE);
    }

    public DungeonPlayType getPlayType() {
        return Optional.ofNullable(this.playType).orElse(DungeonPlayType.DUNGEON_PLAY_TYPE_NONE);
    }

    public DungeonInvolveType getInvolveType() {
        return Optional.ofNullable(this.involveType).orElse(DungeonInvolveType.INVOLVE_NONE);
    }

	@Override
	public void onLoad() {
        Optional.ofNullable(GameData.getRewardPreviewDataMap().get(this.passRewardPreviewId))
            .ifPresent(d -> this.rewardPreviewData = d);
	}
}
