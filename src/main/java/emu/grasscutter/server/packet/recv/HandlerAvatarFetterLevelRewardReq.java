package emu.grasscutter.server.packet.recv;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.net.packet.TypedPacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketAvatarDataNotify;
import emu.grasscutter.server.packet.send.PacketAvatarFetterDataNotify;
import emu.grasscutter.server.packet.send.PacketAvatarFetterLevelRewardRsp;
import emu.grasscutter.server.packet.send.PacketUnlockNameCardNotify;
import org.anime_game_servers.multi_proto.gi.messages.team.avatar.friendship.AvatarFetterLevelRewardReq;
import org.anime_game_servers.game_data_models.gi.data.quest.GainItem;

import lombok.val;

public class HandlerAvatarFetterLevelRewardReq extends TypedPacketHandler<AvatarFetterLevelRewardReq> {
    @Override
    public void handle(GameSession session, byte[] header, AvatarFetterLevelRewardReq req) throws Exception {
        if (req.getFetterLevel() < 10) {
            // You don't have a full level of fetter level, why do you want to get a divorce certificate?
            session.send(new PacketAvatarFetterLevelRewardRsp(req.getAvatarGuid(), req.getFetterLevel()));
        } else {
            long avatarGuid = req.getAvatarGuid();

            val avatar = session
                .getPlayer()
                .getAvatars()
                .getAvatarByGuid(avatarGuid);

            int rewardId = avatar.getNameCardRewardId();

            val rewardData = GameData.getRewardDataMap().get(rewardId);
            int cardId = rewardData.getRewardItemList().stream().findFirst().map(GainItem::getItemId).orElse(0);

            if (session.getPlayer().getNameCardList().contains(cardId)) {
                // Already got divorce certificate.
                session.getPlayer().sendPacket(new PacketAvatarFetterLevelRewardRsp(req.getAvatarGuid(), req.getFetterLevel(), rewardId));
                return;
            }

            session.getPlayer().getInventory().addRewardData(rewardData, ActionReason.FetterLevelReward);
            session.getPlayer().sendPacket(new PacketUnlockNameCardNotify(cardId));
            session.send(new PacketAvatarFetterDataNotify(avatar));
            session.send(new PacketAvatarDataNotify(avatar.getPlayer()));
            session.send(new PacketAvatarFetterLevelRewardRsp(avatarGuid, req.getFetterLevel(), rewardId));
        }
    }
}
