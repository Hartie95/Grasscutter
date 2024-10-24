package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.GadgetData;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.create_config.CreateMonsterEntityConfig;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.EntityType;
import emu.grasscutter.net.packet.TypedPacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketQuestCreateEntityRsp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.general.entity.CreateEntityInfo.Entity.GadgetId;
import org.anime_game_servers.multi_proto.gi.messages.general.entity.CreateEntityInfo.Entity.ItemId;
import org.anime_game_servers.multi_proto.gi.messages.general.entity.CreateEntityInfo.Entity.MonsterId;
import org.anime_game_servers.multi_proto.gi.messages.general.entity.CreateEntityInfo.Entity.NpcId;
import org.anime_game_servers.multi_proto.gi.messages.quest.entities.QuestCreateEntityReq;

import static emu.grasscutter.game.world.SceneHelpers.createEntityFromCreateData;

public class HandlerQuestCreateEntityReq extends TypedPacketHandler<QuestCreateEntityReq> {

    @Override
    public void handle(GameSession session, byte[] header, QuestCreateEntityReq req) throws Exception {
        val entity = req.getEntity();

        if(entity == null || entity.getEntity() == null){
            Grasscutter.getLogger().warn("QuestCreateEntityReq.entity null, either an invalid packet, or the parsing failed");
            return;
        }
        // TODO handle extra fields

        val createdEntityId = createEntityFromCreateData(session.getPlayer(), entity);

        session.send(new PacketQuestCreateEntityRsp(createdEntityId, req));
    }


}
