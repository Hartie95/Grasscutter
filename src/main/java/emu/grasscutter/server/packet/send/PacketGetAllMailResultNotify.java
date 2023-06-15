package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetAllMailResultNotifyOuterClass.GetAllMailResultNotify;
import emu.grasscutter.utils.Utils;
import lombok.val;

import java.time.Instant;
import java.util.List;

/**
 * Used since 3.0 for mail requests.
 */
public class PacketGetAllMailResultNotify extends BasePacket {

    public PacketGetAllMailResultNotify(Player player, boolean isGiftMail) {
        super(PacketOpcodes.GetAllMailResultNotify);

        val packet = GetAllMailResultNotify.newBuilder()
            .setTransaction(player.getUid() + "-" + Utils.getCurrentSeconds() + "-" + 0)
            .setIsCollected(isGiftMail)
            .setPageIndex(1)
            .setTotalPageCount(1);

        val inbox = player.getAllMail();
        if (!isGiftMail && inbox.size() > 0) {
            packet.addAllMailList(inbox.stream()
                .filter(mail -> mail.stateValue == 1)
                .filter(mail -> mail.expireTime > Instant.now().getEpochSecond())
                .map(mail -> mail.toProto(player)).toList());
        } else {
            // Empty mailbox.
            // TODO: Implement the gift mailbox.
            packet.addAllMailList(List.of());
        }

        this.setData(packet.build());
    }
}
