package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetScenePointRspOuterClass.GetScenePointRsp;

public class PacketGetScenePointRsp extends BasePacket {

    public PacketGetScenePointRsp(Player player, int sceneId) {
        super(PacketOpcodes.GetScenePointRsp);

        GetScenePointRsp.Builder p = GetScenePointRsp.newBuilder()
                .setSceneId(sceneId);

        if (GameData.getScenePointIdList().size() > 0) {
            p.addAllUnlockedPointList(player.getUnlockedScenePoints(sceneId));
            p.addAllUnlockAreaList(player.getUnlockedSceneAreas(sceneId));
        }

        this.setData(p);
    }
}
