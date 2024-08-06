package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneInitFinishRsp;

public class PacketSceneInitFinishRsp extends BaseTypedPacket<SceneInitFinishRsp> {

	public PacketSceneInitFinishRsp(Player player) {
        super(new SceneInitFinishRsp(), 11);
        proto.setEnterSceneTtoken(player.getEnterSceneToken());
	}
}
