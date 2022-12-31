package emu.grasscutter.server.packet.send;

import java.util.Arrays;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.common.PointData;
import emu.grasscutter.data.excels.QuestData.QuestContentCondition;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonEntryInfoOuterClass.DungeonEntryInfo;
import emu.grasscutter.net.proto.DungeonEntryInfoRspOuterClass.DungeonEntryInfoRsp;

import java.util.List;
import java.util.Map;

public class PacketDungeonEntryInfoRsp extends BasePacket {
	
	public PacketDungeonEntryInfoRsp(Player player, int sceneId, PointData pointData) {
		super(PacketOpcodes.DungeonEntryInfoRsp);

		// Dungeon Entry might be accessed out of player's scene, i.e. accessing mondstadt
		// from the knight's room
		DungeonEntryInfoRsp.Builder proto = DungeonEntryInfoRsp.newBuilder()
				.setPointId(pointData.getId());
		
		int[] recommendedDungeonId = new int[]{999};
		if (pointData.getDungeonIds() != null) {
			for (int dungeonId : pointData.getDungeonIds()) {
				DungeonEntryInfo info = DungeonEntryInfo.newBuilder().setDungeonId(dungeonId).build();
				if (GameData.getDungeonDataMap().get(dungeonId).getShowLevel() < recommendedDungeonId[0]) {
					recommendedDungeonId[0] = dungeonId;
				}
				proto.addDungeonEntryList(info);
			}
		}

		Map<Integer, Integer> dungeonPlotEntry = GameData.getQuestDungeonEntry(sceneId, pointData.getId());
		if (dungeonPlotEntry != null) {
			dungeonPlotEntry.entrySet().stream().forEach(e -> {
				// key -> subQuestId, value -> dungeonId
				if (player.getQuestManager().getQuestById(e.getKey()) == null
					|| !player.getQuestManager().getQuestById(e.getKey()).isUnfinished()) return;

				if (GameData.getDungeonDataMap().get(e.getValue()).getShowLevel() < recommendedDungeonId[0]) {
					recommendedDungeonId[0] = e.getValue();
				}
				
				player.recordPlotDungeon(e.getKey(), e.getValue());
				proto.addDungeonEntryList(DungeonEntryInfo.newBuilder()
					.setDungeonId(e.getValue())
					.build());
			});
		}
		

		this.setData(proto.setRecommendDungeonId(recommendedDungeonId[0] == 999 ? 0 : recommendedDungeonId[0]).build());
	}
	
	public PacketDungeonEntryInfoRsp() {
		super(PacketOpcodes.DungeonEntryInfoRsp);

		DungeonEntryInfoRsp proto = DungeonEntryInfoRsp.newBuilder()
				.setRetcode(1)
				.build();
		
		this.setData(proto);
	}
}
