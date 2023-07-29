package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.EvtAiSyncCombatThreatInfoNotifyOuterClass.EvtAiSyncCombatThreatInfoNotify;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.game.GameSession;

import java.util.Objects;

@Opcodes(PacketOpcodes.EvtAiSyncCombatThreatInfoNotify)
public class HandlerEvtAiSyncCombatThreatInfoNotify extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        EvtAiSyncCombatThreatInfoNotify notify = EvtAiSyncCombatThreatInfoNotify.parseFrom(payload);

        notify.getCombatThreatInfoMapMap().keySet().stream()
            .map(entityId -> session.getPlayer().getScene().getEntityById(entityId))
            .filter(Objects::nonNull)
            .map(GameEntity::getGroupId)
            .distinct().forEach(groupId -> session.getPlayer().getScene().getScriptManager()
                .callEvent(new ScriptArgs(groupId, EventType.EVENT_MONSTER_BATTLE)));
	}

}
