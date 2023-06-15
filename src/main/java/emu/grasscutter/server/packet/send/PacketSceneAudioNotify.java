package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AbilityChangeNotifyOuterClass.AbilityChangeNotify;
import emu.grasscutter.net.proto.SceneAudioNotifyOuterClass.SceneAudioNotify;
import lombok.val;

import java.util.List;

public class PacketSceneAudioNotify extends BasePacket {

    public PacketSceneAudioNotify(SceneAudioNotify notify) {
        super(PacketOpcodes.SceneAudioNotify);

        this.setData(notify);
    }

    public PacketSceneAudioNotify(int sourceUid, List<Float> param2, List<String> param3, int type, List<Integer> param1) {
        super(PacketOpcodes.SceneAudioNotify);

        val proto = SceneAudioNotify.newBuilder()
            .setSourceUid(sourceUid)
            .addAllParam2(param2)
            .addAllParam3(param3)
            .setType(type)
            .addAllParam1(param1);

        this.setData(proto);
    }
}
