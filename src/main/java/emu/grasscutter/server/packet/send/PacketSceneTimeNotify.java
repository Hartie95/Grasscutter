package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneTimeNotify;

public class PacketSceneTimeNotify extends BaseTypedPacket<SceneTimeNotify> {

    public PacketSceneTimeNotify(Player player) {
        this(player.getScene());
    }

    public PacketSceneTimeNotify(Scene scene) {
        super(new SceneTimeNotify());
        proto.setSceneId(scene.getId());
        proto.setPaused(scene.isPaused());
        proto.setSceneTime(scene.getSceneTime());
    }
}
