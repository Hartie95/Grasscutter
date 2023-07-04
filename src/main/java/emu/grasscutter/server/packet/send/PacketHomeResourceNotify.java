package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeResourceNotifyOuterClass.HomeResourceNotify;

import java.util.List;

public class PacketHomeResourceNotify extends BasePacket {

    public PacketHomeResourceNotify(Player player) {
        super(PacketOpcodes.HomeResourceNotify);
        if (player.getCurrentRealmId() <= 0) return;

        player.getHome().updateResources();
        HomeResourceNotify proto = HomeResourceNotify.newBuilder()
            .setFetterExp(player.getHome().fetterExpResourceToProto())
            .setHomeCoin(player.getHome().homeCoinResourceToProto())
            .build();
        emu.grasscutter.Grasscutter.getLogger().info("Fetter Store Limit: {}", player.getHome().getLevelData().getHomeFetterExpStoreLimit());
        emu.grasscutter.Grasscutter.getLogger().info("Fetter Store Value: {}", player.getHome().getFetterExpStoreValue());
        emu.grasscutter.Grasscutter.getLogger().info("Fetter Refresh Time: {}", player.getHome().getFetterExpRefreshTime());
        emu.grasscutter.Grasscutter.getLogger().info("Home Coin Store Limit: {}", player.getHome().getLevelData().getHomeCoinStoreLimit());
        emu.grasscutter.Grasscutter.getLogger().info("Home Coin Store Value: {}", player.getHome().getHomeCoinStoreValue());
        emu.grasscutter.Grasscutter.getLogger().info("Home Coin Refresh Time: {}", player.getHome().getHomeCoinRefreshTime());
        this.setData(proto);
    }
}
