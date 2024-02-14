package emu.grasscutter.game.entity.create_config;

import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.world.SpawnDataEntry;
import emu.grasscutter.utils.Position;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.anime_game_servers.gi_lua.models.scene.group.SceneObject;
import org.anime_game_servers.multi_proto.gi.messages.general.entity.CreateEntityInfo;

@Data
@NoArgsConstructor @Accessors(chain = true)
public abstract class CreateEntityConfig<T extends CreateEntityConfig<T>> {
    private Object initDataSource = null;
    private Position bornPos = null;
    private Position bornRot = null;
    private Position pos = null;
    private Position rot = null;
    private int blockId;
    private int configId;
    private int groupId;
    private int level = 1;

    protected CreateEntityConfig(Object initDataSource){
        this.pos = new Position();
        this.rot = new Position();
        this.initDataSource = initDataSource;
    }

    protected CreateEntityConfig(SpawnDataEntry spawnDataEntry){
        this.initDataSource = spawnDataEntry;
        this.configId = spawnDataEntry.getConfigId();
        this.groupId = spawnDataEntry.getGroup().getGroupId();
        this.blockId = spawnDataEntry.getGroup().getBlockId();
        this.level = spawnDataEntry.getLevel();
        this.bornPos = spawnDataEntry.getPos();
        this.bornRot = spawnDataEntry.getRot();
        this.pos = bornPos.clone();
        this.rot = bornRot.clone();
    }
    protected CreateEntityConfig(SceneObject object){
        this.initDataSource = object;
        this.blockId = object.getBlockId();
        this.configId = object.getConfigId();
        this.groupId = object.getGroupId();
        this.level = object.getLevel();
        this.bornPos = object.getPos()!= null ? new Position(object.getPos()) : new Position();
        this.bornRot = object.getRot()!= null ? new Position(object.getRot()) : new Position();
        this.pos = bornPos.clone();
        this.rot = bornRot.clone();
    }
    protected CreateEntityConfig(GameEntity<?> parent){
        this.initDataSource = parent;
        this.blockId = parent.getBlockId();
        this.configId = parent.getConfigId();
        this.groupId = parent.getGroupId();
        this.level = parent.getLevel();
    }

    // Creation requested by the client
    protected CreateEntityConfig(CreateEntityInfo createInfo){
        this.initDataSource = createInfo;
        this.bornPos = createInfo.getPos()!= null ? new Position(createInfo.getPos()) : new Position();
        this.bornRot = createInfo.getRot()!= null ? new Position(createInfo.getRot()) : new Position();
        this.pos = bornPos.clone();
        this.rot = bornRot.clone();
        this.blockId = 0;
        this.configId = 0;
        this.groupId = 0;
        this.level = createInfo.getLevel();
    }

    public T setBornPos(Position bornPos) {
        this.bornPos = bornPos;
        if(this.pos == null || this.pos.isDefault()){
            this.pos = bornPos.clone();
        }
        return (T) this;
    }

    public T setBornRot(Position bornRot) {
        this.bornRot = bornRot;
        if(this.rot == null || this.rot.isDefault()){
            this.rot = bornRot.clone();
        }
        return (T) this;
    }

    public T setPos(Position pos) {
        this.pos = pos;
        return (T) this;
    }

    public T setRot(Position rot) {
        this.rot = rot;
        return (T) this;
    }

    public T setLevel(int level) {
        this.level = level;
        return (T) this;
    }
}
