package emu.grasscutter.server.packet.send;

import java.util.Map.Entry;
import java.util.ArrayList;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.TeamInfo;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AvatarTeamOuterClass.AvatarTeam;
import emu.grasscutter.net.proto.AvatarTeamUpdateNotifyOuterClass.AvatarTeamUpdateNotify;

public class PacketAvatarTeamUpdateNotify extends BasePacket {

    public PacketAvatarTeamUpdateNotify(Player player) {
        super(PacketOpcodes.AvatarTeamUpdateNotify);

        AvatarTeamUpdateNotify.Builder proto = AvatarTeamUpdateNotify.newBuilder();
        boolean useTempTeams = false;
        var tempAvatarGuidList = player.getTeamManager().getTrialTeamGuid();
        if (tempAvatarGuidList != null){
            proto.addAllTempAvatarGuidList(new ArrayList<Long>(tempAvatarGuidList.values()));
            useTempTeams = true;
        }
        if (!useTempTeams){
            for (Entry<Integer, TeamInfo> entry : player.getTeamManager().getTeams().entrySet()) {
                TeamInfo teamInfo = entry.getValue();
                proto.putAvatarTeamMap(entry.getKey(), teamInfo.toProto(player));
            }
        }
        this.setData(proto);
    }

    public PacketAvatarTeamUpdateNotify() {
        super(PacketOpcodes.AvatarTeamUpdateNotify);
        this.setData(AvatarTeamUpdateNotify.newBuilder().build());
    }
}
