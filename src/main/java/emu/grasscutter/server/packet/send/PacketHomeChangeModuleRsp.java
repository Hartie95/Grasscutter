package emu.grasscutter.server.packet.send;

import emu.grasscutter.data.GameData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.utils.Position;
import emu.grasscutter.net.proto.HomeChangeModuleRspOuterClass.HomeChangeModuleRsp;


public class PacketHomeChangeModuleRsp extends BasePacket {

    public PacketHomeChangeModuleRsp(Player player, int moduleId) {
        super(PacketOpcodes.HomeChooseModuleRsp);

        player.setCurrentRealmId(moduleId);
        int realmId = player.getHome().getHomeSceneItem().getSceneId();
        Scene scene = player.getWorld().getSceneById(realmId);
        Position pos = scene.getScriptManager().getConfig().born_pos;

        boolean result = player.getWorld().transferPlayerToScene(player, realmId, pos);
        // weird function name but i think its necessary to resend all the information when moving to new realm
        HomeChangeModuleRsp.Builder proto = HomeChangeModuleRsp.newBuilder();
        if (result) {
            player.getHome().sendInitialPackets();
            proto.setRetcode(Retcode.RET_SUCC.getNumber())
                .setTargetModuleId(moduleId);
        } else {
            proto.setRetcode(Retcode.RET_HOME_ALREADY_IN_TARGET_SCENE.getNumber());
        }

        this.setData(proto.build());
    }
}
