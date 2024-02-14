package emu.grasscutter.game.world;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.create_config.CreateMonsterEntityConfig;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.general.entity.CreateEntityInfo;

public class SceneHelpers {
    public static int createEntityFromCreateData(Player player, CreateEntityInfo createEntityInfo){
        val scene = player.getWorld().getSceneById(createEntityInfo.getSceneId());
        val entityIdInfo = parseIdInfo(createEntityInfo.getEntity());
        if(entityIdInfo == null){
            return -1;
        }

        GameEntity<?> gameEntity = null;
        switch (entityIdInfo.getType()){
            case GADGET -> {
                val gadgetId = entityIdInfo.getId();
                val createConfig = new CreateGadgetEntityConfig(createEntityInfo, gadgetId);
                val gadgetData = createConfig.getGadgetData();
                val gadgetType = gadgetData != null ? gadgetData.getType() : EntityType.None;
                gameEntity = switch (gadgetType){
                    case Vehicle -> new EntityVehicle(scene, player, createConfig);
                    default -> new EntityGadget(scene, createConfig);
                };
            }
            case ITEM -> {
                val itemId = entityIdInfo.getId();
                val itemData = GameData.getItemDataMap().get(itemId);
                val createConfig = new CreateGadgetEntityConfig(createEntityInfo, itemData);
                gameEntity = new EntityItem(scene, createConfig);
            }
            case MONSTER -> {
                val monsterId = entityIdInfo.getId();
                val monsterData = GameData.getMonsterDataMap().get(monsterId);
                val createConfig = new CreateMonsterEntityConfig(createEntityInfo, monsterId);
                createConfig.setMonsterData(monsterData);
                gameEntity = new EntityMonster(scene, createConfig);
            }
            case NPC -> {
            }
        }

        if(gameEntity != null){
            scene.addEntity(gameEntity);
        }

        return gameEntity!=null ? gameEntity.getId() : -1;
    }

    enum EntityIdType{
        MONSTER,
        NPC,
        GADGET,
        ITEM
    }

    @Data
    @AllArgsConstructor
    public static class EntityIdInfo{
        private int id;
        private EntityIdType type;
    }

    private static EntityIdInfo parseIdInfo(CreateEntityInfo.Entity<?> entityIdInfo){
        if(entityIdInfo instanceof CreateEntityInfo.Entity.MonsterId monsterId){
            return new EntityIdInfo(monsterId.getValue(), EntityIdType.MONSTER);
        } else if(entityIdInfo instanceof CreateEntityInfo.Entity.NpcId npcId){
            return new EntityIdInfo(npcId.getValue(), EntityIdType.NPC);
        } else if(entityIdInfo instanceof CreateEntityInfo.Entity.GadgetId gadgetId){
            return new EntityIdInfo(gadgetId.getValue(), EntityIdType.GADGET);
        } else if(entityIdInfo instanceof CreateEntityInfo.Entity.ItemId itemId){
            return new EntityIdInfo(itemId.getValue(), EntityIdType.ITEM);
        } else {
            Grasscutter.getLogger().error("Unknown entity type: {}", entityIdInfo.getClass().getName());
            return null;
        }
    }
}
