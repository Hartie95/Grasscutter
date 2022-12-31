package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.dungeons.enums.*;
import lombok.Getter;

import java.util.List;

@ResourceType(name = "DungeonExcelConfigData.json")
public class DungeonData extends GameResource {
	private int id;
	@Getter private int sceneId;
	@Getter private int showLevel;
    private DungeonType type;
    private DungeonSubType subType;
    private DungeonPlayType playType;
    private DungeonInvolveType involveType;
	@Getter private int limitLevel;
	@Getter private int passCond;
	@Getter private int reviveMaxCount;
	@Getter private int settleCountdownTime;
	@Getter private int failSettleCountdownTime;
	@Getter private int quitSettleCountdownTime;
	@Getter private List<SettleShowType> settleShows;
	@Getter private int passRewardPreviewID;
    @Getter private int statueCostID;
    @Getter private int statueCostCount;

    @Getter private RewardPreviewData rewardPreviewData;
    @Getter private String entryPicPath;
    @Getter private List<Integer> enterCostItems;

	@Override
	public int getId() {
		return this.id;
	}

    public DungeonType getType() {
        if (type == null) {
            return DungeonType.DUNGEON_NONE;
        }
        return type;
    }

    public DungeonSubType getSubType() {
        if (subType == null) {
            return DungeonSubType.DUNGEON_SUB_NONE;
        }
        return subType;
    }

    public DungeonPlayType getPlayType() {
        if (playType == null) {
            return DungeonPlayType.DUNGEON_PLAY_TYPE_NONE;
        }
        return playType;
    }

    public DungeonInvolveType getInvolveType() {
        if (involveType == null) {
            return DungeonInvolveType.INVOLVE_NONE;
        }
        return involveType;
    }

	@Override
	public void onLoad() {
		if (this.passRewardPreviewID > 0) {
			this.rewardPreviewData = GameData.getRewardPreviewDataMap().get(this.passRewardPreviewID);
		}
	}
}
