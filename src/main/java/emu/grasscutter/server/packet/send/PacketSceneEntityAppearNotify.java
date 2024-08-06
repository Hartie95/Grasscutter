package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneEntityAppearNotify;
import org.anime_game_servers.multi_proto.gi.messages.scene.VisionType;

import java.util.Collection;
import java.util.List;

public class PacketSceneEntityAppearNotify extends BaseTypedPacket<SceneEntityAppearNotify> {

	public PacketSceneEntityAppearNotify(GameEntity entity) {
        super(new SceneEntityAppearNotify(), true);
        proto.setAppearType(VisionType.VISION_BORN);
        proto.setEntityList(List.of(entity.toProto()));
	}

	public PacketSceneEntityAppearNotify(GameEntity entity, VisionType vision, int param) {
        super(new SceneEntityAppearNotify(), true);
        proto.setAppearType(vision);
        proto.setParam(param);
        proto.setEntityList(List.of(entity.toProto()));
	}

	public PacketSceneEntityAppearNotify(Player player) {
		this(player.getTeamManager().getCurrentAvatarEntity());
	}

	public PacketSceneEntityAppearNotify(Collection<? extends GameEntity> entities, VisionType visionType) {
        super(new SceneEntityAppearNotify(), true);
        proto.setAppearType(visionType);
		proto.setEntityList(entities.stream().map(GameEntity::toProto).toList());
	}
}
