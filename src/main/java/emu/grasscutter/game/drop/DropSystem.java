package emu.grasscutter.game.drop;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.common.DropItemData;
import emu.grasscutter.data.excels.DropSubTableData;
import emu.grasscutter.data.excels.DropTableData;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.server.game.BaseGameSystem;
import emu.grasscutter.server.game.GameServer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.Random;

public class DropSystem extends BaseGameSystem {
    private final Int2ObjectMap<DropSubTableData> dropSubTable;
    private final Int2ObjectMap<DropTableData> dropTable;
    private final Random rand;

    public DropSystem(GameServer server) {
        super(server);
        rand = new Random();
        dropTable = GameData.getDropTableDataMap();
        dropSubTable = GameData.getDropSubTableDataMap();
    }

    public boolean handleChestDrop(int chestDropId, int dropCount, int level, GameEntity bornFrom) {
        //TODO:not clear the usage of level field.
        Grasscutter.getLogger().info("ChestDrop:chest_drop_id={},drop_count={},level={}", chestDropId, dropCount, level);
        return processDrop(chestDropId, dropCount, bornFrom);
    }

    public boolean handleChestDrop(String dropTag, int level, GameEntity bornFrom) {
        Grasscutter.getLogger().info("ChestDrop:drop_tag={},level={}", dropTag, level);
        //TODO
        return false;
    }

    private boolean processDrop(int dropId, int count, GameEntity bornFrom) {
        if (!dropTable.containsKey(dropId)) return false;
        var dropData = dropTable.get(dropId);
        if (dropData.getNodeType() != 1) return false;
        processSubDrop(dropData, count, bornFrom,dropData.isFallToGround());
        return true;
    }

    private void processSubDrop(DropSubTableData dropData, int count, GameEntity bornFrom,boolean fallToGround) {
        //TODO:Not clear on the meaning of some fields,like "dropLevel".Will ignore them.
        //TODO:solve drop limits,like everydayLimit.

        if (dropData.getRandomType() == 0) {
            int weightSum = 0;
            for (var i : dropData.getDropVec()) {
                int id = i.getId();
                if (id == 0) continue;
                weightSum += i.getWeight();
            }
            int weight = rand.nextInt(weightSum);
            int sum = 0;
            for (var i : dropData.getDropVec()) {
                int id = i.getId();
                if (id == 0) continue;
                sum += i.getWeight();
                if (weight < sum) {
                    //win the item
                    int amount = calculateDropAmount(i) * count;
                    if (dropSubTable.containsKey(id)) {
                        processSubDrop(dropSubTable.get(id), amount, bornFrom,fallToGround);
                    } else {
                        if(fallToGround) dropItem(id, amount, bornFrom);
                        else giveItem(id,amount,bornFrom);
                    }
                    break;
                }
            }
        } else if (dropData.getRandomType() == 1) {
            for (var i : dropData.getDropVec()) {
                int id = i.getId();
                if (id == 0) continue;
                if (rand.nextInt(10000) < i.getWeight()) {
                    int amount = calculateDropAmount(i) * count;
                    if (dropSubTable.containsKey(id)) {
                        processSubDrop(dropSubTable.get(id), amount, bornFrom,fallToGround);
                    } else {
                        if(fallToGround) dropItem(id, amount, bornFrom);
                        else giveItem(id,amount,bornFrom);
                    }
                }
            }
        }
    }

    private int calculateDropAmount(DropItemData i) {
        int amount = 0;
        if (i.getCountRange().contains(";")) {
            String[] ranges = i.getCountRange().split(";");
            amount = rand.nextInt(Integer.parseInt(ranges[0]), Integer.parseInt(ranges[1]) + 1);
        } else if (i.getCountRange().contains(".")) {
            double expectAmount = Double.parseDouble(i.getCountRange());
            int chance = (int) expectAmount + 1;
            while ((chance--) > 0) {
                if (rand.nextDouble() < expectAmount / chance) amount++;
            }
        } else {
            amount = Integer.parseInt(i.getCountRange());
        }
        return amount;
    }

    /**
     * @param broadcast Whether other players in the scene could see the drop items.
     */
    private void dropItem(int itemId, int amount, GameEntity bornFrom, boolean broadcast) {
        //TODO:more fluent drop
        Grasscutter.getLogger().info("Drop:{}*{}", itemId, amount);
        //TODO:should not broadcast chest item drop to other players.
        //TODO:send ItemAddHintNotify on auto-pick items like 102,202,etc.
        switch (itemId){
            case 201:
            case 202:
            case 102:
                bornFrom.getScene().getWorld().getHost().getInventory().addItem(itemId,amount);
            default:
                bornFrom.getScene().addItemEntity(itemId,amount,bornFrom);
        }
    }

    private void dropItem(int itemId, int amount, GameEntity bornFrom) {
        dropItem(itemId, amount, bornFrom, false);
    }
    private void giveItem(int itemId,int amount,GameEntity bornFrom,boolean broadcast){
        bornFrom.getScene().getWorld().getHost().getInventory().addItem(itemId,amount);
    }
    private void giveItem(int itemId,int amount,GameEntity bornFrom){
        giveItem(itemId,amount,bornFrom,false);
    }
}
