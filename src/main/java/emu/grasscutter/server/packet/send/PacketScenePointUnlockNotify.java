package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ScenePointUnlockNotifyOuterClass.ScenePointUnlockNotify;

public class PacketScenePointUnlockNotify extends BasePacket {
    public PacketScenePointUnlockNotify(int sceneId, int pointId) {
        this(sceneId, pointId, false);
    }

    public PacketScenePointUnlockNotify(int sceneId, int pointId, boolean lock) {
        super(PacketOpcodes.ScenePointUnlockNotify);

        ScenePointUnlockNotify.Builder p = ScenePointUnlockNotify.newBuilder()
                .setSceneId(sceneId)
                .addPointList(pointId);
        if(lock)
            p.addLockedPointList(pointId);
        else
            p.addUnhidePointList(pointId);

        this.setData(p);
    }

    public PacketScenePointUnlockNotify(int sceneId, Iterable<Integer> pointIds) {
        this(sceneId, pointIds, false);
    }

    public PacketScenePointUnlockNotify(int sceneId, Iterable<Integer> pointIds, boolean lock) {
        super(PacketOpcodes.ScenePointUnlockNotify);

        ScenePointUnlockNotify.Builder p = ScenePointUnlockNotify.newBuilder()
                .setSceneId(sceneId)
                .addAllPointList(pointIds);
        if(lock)
            p.addAllLockedPointList(pointIds);
        else
            p.addAllUnhidePointList(pointIds);

        this.setData(p);
    }
}
