package emu.grasscutter.game.entity.create_config;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.binout.config.ConfigEntityMonster;
import emu.grasscutter.data.excels.MonsterData;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.world.SpawnDataEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.anime_game_servers.gi_lua.models.scene.group.SceneMonster;
import org.anime_game_servers.multi_proto.gi.messages.general.entity.CreateEntityInfo;

@Getter @Setter @AllArgsConstructor @Accessors(chain = true)
public class CreateMonsterEntityConfig extends CreateEntityConfig<CreateMonsterEntityConfig> {
    private int monsterId;
    private MonsterData monsterData = null;
    private ConfigEntityMonster configEntity = null;
    private int dropId;
    private int aiId = 0;
    private int poseId = 0;
    private int campId = 0;
    //TODO

    public CreateMonsterEntityConfig(int monsterId){
        this.monsterId = monsterId;
    }
    public CreateMonsterEntityConfig(MonsterData monsterData){
        this.monsterId = monsterData.getId();
        this.monsterData = monsterData;
    }

    public CreateMonsterEntityConfig(GameEntity<?> parent, int monsterId){
        super(parent);
        this.monsterId = monsterId;
        setupMonsterData(monsterId);
        setupConfigEntity(monsterId);
    }

    public CreateMonsterEntityConfig(CreateEntityInfo requestedConfig, int monsterId){
        super(requestedConfig);
        this.monsterId = monsterId;
        setupMonsterData(monsterId);
        setupConfigEntity(monsterId);
    }

    public CreateMonsterEntityConfig(SceneMonster monster){
        super(monster);
        this.monsterId = monster.getMonsterId();
        this.dropId = monster.getDropId();
        this.aiId = monster.getAiConfigId();
        this.poseId = monster.getPoseId();
        // TODO

        setupMonsterData(monsterId);
        setupConfigEntity(monsterId);
    }

    public CreateMonsterEntityConfig(SpawnDataEntry spawnDataEntry){
        super(spawnDataEntry);
        this.monsterId = spawnDataEntry.getMonsterId();
        this.poseId = spawnDataEntry.getPoseId();
        // TODO

        setupMonsterData(monsterId);
        setupConfigEntity(monsterId);
    }

    private void setupConfigEntity(int monsterId){
        val monsterMapping = GameData.getMonsterMappingMap().get(monsterId);
        if(monsterMapping != null && monsterMapping.getMonsterJson() != null) {
            this.configEntity = GameData.getMonsterConfigData().get(monsterMapping.getMonsterJson());
        }
    }

    private void setupMonsterData(int monsterId){
        this.monsterData = GameData.getMonsterDataMap().get(monsterId);
        if(this.monsterData == null){
            return;
        }
        if(campId == 0) {
            this.campId = this.monsterData.getCampID();
        }
    }
}
