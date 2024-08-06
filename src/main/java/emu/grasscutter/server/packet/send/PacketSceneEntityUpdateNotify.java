package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.scene.SceneEntityUpdateNotify;
import org.anime_game_servers.multi_proto.gi.messages.scene.VisionType;

import java.util.Collection;
import java.util.List;

public class PacketSceneEntityUpdateNotify extends BaseTypedPacket<SceneEntityUpdateNotify> {

	public PacketSceneEntityUpdateNotify(GameEntity entity) {
		super(new SceneEntityUpdateNotify(), true);
        proto.setEntityList(List.of(entity.toProto()));
        proto.setAppearType(VisionType.VISION_BORN);
	}

	public PacketSceneEntityUpdateNotify(GameEntity entity, VisionType vision, int param) {
        super(new SceneEntityUpdateNotify(), true);
        proto.setEntityList(List.of(entity.toProto()));
        proto.setAppearType(vision);
        proto.setParam(param);
	}

	public PacketSceneEntityUpdateNotify(Player player) {
		this(player.getTeamManager().getCurrentAvatarEntity());
	}

	public PacketSceneEntityUpdateNotify(Collection<? extends GameEntity> entities, VisionType visionType) {
		super(new SceneEntityUpdateNotify(), true);
        proto.setEntityList(entities.stream().map(GameEntity::toProto).toList());
        proto.setAppearType(visionType);
	}
}
