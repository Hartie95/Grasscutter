package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.scene.UnlockTransPointRsp;

public class PacketUnlockTransPointRsp extends BaseTypedPacket<UnlockTransPointRsp> {
    public PacketUnlockTransPointRsp(boolean unlocked) {
        super(new UnlockTransPointRsp());
        proto.setRetcode(unlocked ? RetcodeOuterClass.Retcode.RET_SUCC_VALUE : RetcodeOuterClass.Retcode.RET_FAIL_VALUE);
    }
}
