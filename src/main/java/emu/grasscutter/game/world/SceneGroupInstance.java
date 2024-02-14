package emu.grasscutter.game.world;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import emu.grasscutter.game.entity.EntityBaseGadget;
import emu.grasscutter.game.entity.EntityGadget;
import org.anime_game_servers.gi_lua.models.SceneGroupUserData;
import org.anime_game_servers.gi_lua.models.scene.group.SceneGadget;
import org.anime_game_servers.gi_lua.models.scene.group.SceneGroup;
import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Entity(value = "group_instances", useDiscriminator = false)
public class SceneGroupInstance implements SceneGroupUserData {
    @Id private ObjectId id;

    @Indexed private int ownerUid; //This group is owned by the host player
    @Getter private int groupId;

    @Getter private transient SceneGroup luaGroup;
    @Getter @Setter private int targetSuiteId;
    @Getter @Setter private int activeSuiteId;
    @Getter private Set<Integer> deadEntities; //Config_ids
    private boolean isCached;

    @Getter private Map<Integer, Integer> cachedGadgetStates;
    @Getter private Map<String, Integer> cachedVariables;

    @Getter @Setter private int lastTimeRefreshed;
    @Nullable @Getter @Setter private Boolean isReplaceable;

    public SceneGroupInstance(SceneGroup group, Player owner) {
        this.luaGroup = group;
        this.groupId = group.getGroupInfo().getId();
        this.targetSuiteId = 0;
        this.activeSuiteId = 0;
        this.lastTimeRefreshed = 0;
        this.ownerUid = owner.getUid();
        this.deadEntities = new HashSet<>();
        this.cachedGadgetStates = new ConcurrentHashMap<>();
        this.cachedVariables = new ConcurrentHashMap<>();

        this.isCached = false; //This is true when the group is not loaded on scene but caches suite data
    }

    @Deprecated  // Morphia only!
    SceneGroupInstance(){
        this.cachedVariables = new ConcurrentHashMap<>();
        this.deadEntities = new HashSet<>();
        this.cachedGadgetStates = new ConcurrentHashMap<>();
    }

    public void setLuaGroup(SceneGroup group) {
        this.luaGroup = group;
        this.groupId = group.getGroupInfo().getId();
        initWithSceneGroup(group.getGroupInfo());
    }

    public boolean isCached() {
        return this.isCached;
    }

    public void setCached(boolean value) {
        this.isCached = value;
        save(); //Save each time a group is registered or unregistered
    }

    public void cacheGadgetState(EntityGadget g) {
        if(g.isPersistent()) //Only cache when is persistent
            cachedGadgetStates.put(g.getConfigId(), g.getState());
    }

    public int getCachedGadgetState(SceneGadget g) {
        return cachedGadgetStates.getOrDefault(g.getConfigId(), g.getState());
    }

    public void save() {
        DatabaseHelper.saveGroupInstance(this);
    }

    @Nullable
    @Override
    public Boolean isReplaceable() {
        return isReplaceable;
    }

    @Override
    public void setReplaceable(@Nullable Boolean isReplaceable) {
        this.isReplaceable = isReplaceable;
    }
}
