package emu.grasscutter.server.packet.recv;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.quest.TeleportData;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketQuestTransmitRsp;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.QuestTransmitReqOuterClass.QuestTransmitReq;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import lombok.val;

@Opcodes(PacketOpcodes.QuestTransmitReq)
public class HandlerQuestTransmitReq extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		val req = QuestTransmitReq.parseFrom(payload);
        val player = session.getPlayer();
        final List<Position> posAndRot = new ArrayList<>();
        int sceneId = Optional.ofNullable(GameData.getTeleportDataMap().get(req.getQuestId()))
            .map(TeleportData::getTransmit_points).stream().flatMap(List::stream).findFirst()
            .map(TeleportData.TransmitPoint::getScene_id).orElse(3);

        val result = Optional.ofNullable(player.getQuestManager().getQuestById(req.getQuestId()))
            .map(GameQuest::getMainQuest).filter(mainQuest -> mainQuest.hasTeleportPostion(req.getQuestId(), posAndRot))
            .filter(mainQuest -> posAndRot.size() > 1) // make sure there is pos and rot
            .filter(mainQuest -> player.getWorld().transferPlayerToScene(
                player, sceneId, TeleportType.CLIENT, posAndRot.get(0), posAndRot.get(1)))
            .isPresent();

		session.send(new PacketQuestTransmitRsp(result, req));
	}
}
