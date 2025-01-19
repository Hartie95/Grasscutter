package emu.grasscutter.game.entity;

import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.utils.Position;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import lombok.Getter;
import org.anime_game_servers.gi_lua.models.scene.group.SceneRegion;
import org.anime_game_servers.multi_proto.gi.messages.scene.entity.SceneEntityInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class EntityRegion extends GameEntity{
    private final Position position;
    private boolean hasNewEntities;
    private boolean entityLeave;
    private final Set<GameEntity> entities; // Ids of entities inside this region
    private final Set<GameEntity> notContainEntities; // Ids of entities outside this region
    private final Set<GameEntity> newEntities; // Ids that entered this region since the last check
    private final Set<GameEntity> leftEntities; // Ids that left this region since the last check
    private final SceneRegion metaRegion;

    public EntityRegion(Scene scene, SceneRegion region) {
        super(scene);
        this.id = getScene().getWorld().getNextEntityId(EntityIdType.REGION);
        setGroupId(region.getGroupId());
        setBlockId(region.getBlockId());
        setConfigId(region.getConfigId());
        this.position = new Position(region.getPos());
        this.entities = ConcurrentHashMap.newKeySet();
        this.notContainEntities = ConcurrentHashMap.newKeySet();
        this.newEntities = ConcurrentHashMap.newKeySet();
        this.leftEntities = ConcurrentHashMap.newKeySet();
        this.metaRegion = region;
    }

    public void addEntity(GameEntity entity) {
        if (this.entities.contains(entity)) {
            return;
        }
        this.entities.add(entity);
        if (this.notContainEntities.remove(entity)) {
            this.newEntities.add(entity);
            this.hasNewEntities = true;
        }
    }

    @Override
    public int getEntityTypeId() {
        return metaRegion.getConfigId();
    }

    public void resetNewEntities() {
        this.hasNewEntities = false;
        this.newEntities.clear();
    }

    public void removeEntity(GameEntity entity) {
        if (this.notContainEntities.contains(entity)) {
            return;
        }
        this.notContainEntities.add(entity);
        if (this.entities.remove(entity)) {
            this.leftEntities.add(entity);
            this.entityLeave = true;
        }
    }

    public void resetEntityLeave() {
        this.entityLeave = false;
        this.leftEntities.clear();
    }

    public void clearDeadEntities() {
        entities.removeAll(entities.stream()
            .filter(entity -> this.getScene().getEntityById(entity.id) == null)
            .collect(Collectors.toSet()));
        notContainEntities.removeAll(notContainEntities.stream()
            .filter(entity -> this.getScene().getEntityById(entity.id) == null)
            .collect(Collectors.toSet()));
    }

    @Override public Int2FloatMap getFightProperties() {return null;}

    @Override public Position getPosition() {return position;}

    @Override public Position getRotation() {return null;}

    @Override
    public SceneEntityInfo toProto() {
        /**
         * The Region Entity would not be sent to client.
         */
        return null;
    }
}
