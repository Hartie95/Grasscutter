package emu.grasscutter.game.managers.deforestation;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.entity.EntityItem;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.player.BasePlayerManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import org.anime_game_servers.multi_proto.gi.messages.scene.HitTreeNotify;
import org.anime_game_servers.multi_proto.gi.messages.general.Vector;
import emu.grasscutter.utils.Position;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;

public class DeforestationManager extends BasePlayerManager {
    final static int RECORD_EXPIRED_SECONDS = 60*5; // 5 min
    final static int RECORD_MAX_TIMES = 3; // max number of wood
    final static int RECORD_MAX_TIMES_OTHER_HIT_TREE = 10; // if hit 10 times other trees, reset wood

    private final ArrayList<HitTreeRecord> currentRecord;
    private final static HashMap<Integer, Integer> ColliderTypeToWoodItemID = new HashMap<>();

    static {
        /* define wood types which reflected to item id*/
        ColliderTypeToWoodItemID.put(1,101301);
        ColliderTypeToWoodItemID.put(2,101302);
        ColliderTypeToWoodItemID.put(3,101303);
        ColliderTypeToWoodItemID.put(4,101304);
        ColliderTypeToWoodItemID.put(5,101305);
        ColliderTypeToWoodItemID.put(6,101306);
        ColliderTypeToWoodItemID.put(7,101307);
        ColliderTypeToWoodItemID.put(8,101308);
        ColliderTypeToWoodItemID.put(9,101309);
        ColliderTypeToWoodItemID.put(10,101310);
        ColliderTypeToWoodItemID.put(11,101311);
        ColliderTypeToWoodItemID.put(12,101312);
        ColliderTypeToWoodItemID.put(13,101313);
        ColliderTypeToWoodItemID.put(14,101314);
        ColliderTypeToWoodItemID.put(15,101315);
        ColliderTypeToWoodItemID.put(16,101316);
        ColliderTypeToWoodItemID.put(17,101317);
    }

    public DeforestationManager(Player player) {
        super(player);
        this.currentRecord = new ArrayList<>();
    }
    public void resetWood() {
        synchronized (currentRecord) {
            currentRecord.clear();
        }
    }

    public void onDeforestationInvoke(HitTreeNotify hit) {
        synchronized (currentRecord) {
            //Grasscutter.getLogger().info("onDeforestationInvoke! Wood records {}", currentRecord);
            Vector hitPosition = hit.getTreePos();
            int woodType = hit.getTreeType();
            if (ColliderTypeToWoodItemID.containsKey(woodType)) {// is a available wood type
                Scene scene = player.getScene();
                int itemId = ColliderTypeToWoodItemID.get(woodType);
                int positionHash = hitPosition.hashCode();
                HitTreeRecord record = searchRecord(positionHash);
                if (record == null) {
                    record = new HitTreeRecord(positionHash);
                }else {
                    currentRecord.remove(record);// move it to last position
                }
                currentRecord.add(record);
                if (currentRecord.size()>RECORD_MAX_TIMES_OTHER_HIT_TREE) {
                    currentRecord.remove(0);
                }
                if (record.record()) {
                    val itemData = GameData.getItemDataMap().get(itemId);
                    val createConfig = new CreateGadgetEntityConfig(itemData, 1)
                        .setShareItem(false)
                        .setBornPos(new Position(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ()));
                    EntityItem entity = new EntityItem(scene, createConfig);
                    scene.addEntity(entity);
                }
                //record.record()=false : too many wood they have deforested, no more wood dropped!
            } else {
                Grasscutter.getLogger().warn("No wood type {} found.", woodType);
            }
        }
        // unknown wood type
    }
    private HitTreeRecord searchRecord(int id) {
        for (HitTreeRecord record : currentRecord) {
            if (record.getUnique() == id) {
                return record;
            }
        }
        return null;
    }
}
