package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.battle.event.EvtAvatarLockChairReq;
import org.anime_game_servers.multi_proto.gi.messages.battle.event.EvtAvatarLockChairRsp;

public class PacketEvtAvatarLockChairRsp extends BaseTypedPacket<EvtAvatarLockChairRsp> {
    public PacketEvtAvatarLockChairRsp(int clientSequence, EntityAvatar entityAvatar, EvtAvatarLockChairReq lockChairReq) {
        super(new EvtAvatarLockChairRsp());
        proto.setChairId(lockChairReq.getChairId());
        proto.setEntityId(entityAvatar.getId());
        proto.setPosition(lockChairReq.getPosition());
    }
}
