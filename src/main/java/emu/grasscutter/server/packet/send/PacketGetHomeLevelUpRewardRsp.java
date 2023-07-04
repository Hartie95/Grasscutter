package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.server.packet.send.PacketPlayerHomeCompInfoNotify;
import emu.grasscutter.net.proto.GetHomeLevelUpRewardRspOuterClass.GetHomeLevelUpRewardRsp;

import java.util.List;

public class PacketGetHomeLevelUpRewardRsp extends BasePacket {

    public PacketGetHomeLevelUpRewardRsp(Player player, int level) {
        super(PacketOpcodes.GetHomeLevelUpRewardRsp);
        if (!player.getHome().getLevelUpReward(level)) return;

        player.sendPacket(new PacketPlayerHomeCompInfoNotify(player));
        this.setData(GetHomeLevelUpRewardRsp.newBuilder().setLevel(level).build());
    }
}
