package emu.grasscutter.game.drop;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.DataLoader;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.common.DropItemData;
import emu.grasscutter.data.excels.DropSubTableData;
import emu.grasscutter.data.excels.DropTableData;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.MaterialType;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.server.game.BaseGameSystem;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.server.packet.send.PacketItemAddHintNotify;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.*;

public class DropSystem extends BaseGameSystem {
    private final Int2ObjectMap<DropSubTableData> dropSubTable;
    private final Int2ObjectMap<DropTableData> dropTable;
    private final Map<String, List<ChestDropData>> chestReward;
    private final Random rand;

    public DropSystem(GameServer server) {
        super(server);
        rand = new Random();
        dropTable = GameData.getDropTableDataMap();
        dropSubTable = GameData.getDropSubTableDataMap();
        chestReward = new HashMap<>();
        try {
            List<ChestDropData> dataList = DataLoader.loadList("ChestDrop.json", ChestDropData.class);
            for (var i : dataList) {
                if (!chestReward.containsKey(i.getIndex())) {
                    chestReward.put(i.getIndex(), new ArrayList<>());
                }
                chestReward.get(i.getIndex()).add(i);
            }
        } catch (Exception e) {
            Grasscutter.getLogger().error("Unable to load chest drop data.Please place ChestDrop.json in data folder.");
        }

    }

    public boolean handleChestDrop(int chestDropId, int dropCount, GameEntity bornFrom) {
        Grasscutter.getLogger().info("ChestDrop:chest_drop_id={},drop_count={}", chestDropId, dropCount);
        return processDrop(chestDropId, dropCount, ActionReason.OpenChest, bornFrom, bornFrom.getWorld().getHost(), false);
    }

    public boolean handleChestDrop(String dropTag, int level, GameEntity bornFrom) {
        Grasscutter.getLogger().info("ChestDrop:drop_tag={},level={}", dropTag, level);
        if (!chestReward.containsKey(dropTag)) return false;
        var rewardList = chestReward.get(dropTag);
        ChestDropData dropData = null;
        int minLevel = 0;
        for (var i : rewardList) {
            if (level >= i.getMinLevel() && i.getMinLevel() > minLevel) {
                minLevel = i.getMinLevel();
                dropData = i;
            }
        }
        if (dropData == null) return false;
        return processDrop(dropData.getDropId(), dropData.getDropCount(), ActionReason.OpenChest, bornFrom, bornFrom.getWorld().getHost(), false);
    }

    private boolean processDrop(int dropId, int count, ActionReason reason, GameEntity bornFrom, Player player, boolean share) {
        if (!dropTable.containsKey(dropId)) return false;
        var dropData = dropTable.get(dropId);
        if (dropData.getNodeType() != 1) return false;
        List<GameItem> items = new ArrayList<>();
        processSubDrop(dropData, count, items);
        if (dropData.isFallToGround()) {
            dropItems(items, reason, bornFrom, player, share);
        } else {
            giveItems(items, reason, player, share);
        }
        return true;
    }

    private void processSubDrop(DropSubTableData dropData, int count, List<GameItem> items) {
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
                        processSubDrop(dropSubTable.get(id), amount, items);
                    } else {
                        items.add(new GameItem(id, amount));
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
                        processSubDrop(dropSubTable.get(id), amount, items);
                    } else {
                        items.add(new GameItem(id, amount));
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
     * @param share Whether other players in the scene could see the drop items.
     */
    private void dropItem(int itemId, int amount, ActionReason reason, Player player, GameEntity bornFrom, boolean share) {
        if (GameData.getItemDataMap().get(itemId).getMaterialType() == MaterialType.MATERIAL_ADSORBATE) {
            //auto-pick items
            giveItem(itemId, amount, reason, player, share);
        } else {
            player.getScene().addDropEntity(new GameItem(itemId, amount), bornFrom, player, share);
        }
    }

    private void dropItems(List<GameItem> items, ActionReason reason, GameEntity bornFrom, Player player, boolean share) {
        for (var i : items) {
            if (GameData.getItemDataMap().get(i.getItemId()).getMaterialType() == MaterialType.MATERIAL_ADSORBATE) {
                //auto-pick items
                giveItem(i.getItemId(), i.getCount(), reason, player, share);
                items.remove(i);
            }
        }
        player.getScene().addDropEntities(items, bornFrom, player, share);
    }

    private void giveItem(int itemId, int amount, ActionReason reason, Player player, boolean share) {
        if (share) {
            for (var p : player.getScene().getPlayers()) {
                p.getInventory().addItem(itemId, amount, reason);
                p.sendPacket(new PacketItemAddHintNotify(new GameItem(itemId, amount), reason));
            }
        } else {
            player.getInventory().addItem(itemId, amount, reason);
            player.sendPacket(new PacketItemAddHintNotify(new GameItem(itemId, amount), reason));
        }
    }

    private void giveItems(List<GameItem> items, ActionReason reason, Player player, boolean share) {
        if (share) {
            for (var p : player.getScene().getPlayers()) {
                p.getInventory().addItems(items, reason);
                p.sendPacket(new PacketItemAddHintNotify(items, reason));
            }
        } else {
            player.getInventory().addItems(items, reason);
            player.sendPacket(new PacketItemAddHintNotify(items, reason));
        }
    }
}
