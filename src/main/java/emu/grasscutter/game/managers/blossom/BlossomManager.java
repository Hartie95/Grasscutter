package emu.grasscutter.game.managers.blossom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameDepot;
import emu.grasscutter.data.excels.RewardPreviewData;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.entity.gadget.GadgetWorktop;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.managers.ResinManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.game.world.SpawnDataEntry;
import emu.grasscutter.game.world.SpawnDataEntry.SpawnGroupEntry;
import emu.grasscutter.net.proto.BlossomBriefInfoOuterClass.BlossomBriefInfo;
import emu.grasscutter.net.proto.VisionTypeOuterClass;
import emu.grasscutter.server.packet.send.PacketBlossomBriefInfoNotify;
import emu.grasscutter.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@Setter
public class BlossomManager {
    public BlossomManager(Scene scene) {
        this.scene = scene;
    }

    private final Scene scene;
    private final List<BlossomActivity> blossomActivities = new ArrayList<>();
    private final List<BlossomActivity> activeChests = new ArrayList<>();
    private final List<EntityGadget> createdEntity = new ArrayList<>();

    private final List<SpawnDataEntry> blossomConsumed = new ArrayList<>();

    public void onTick() {
        synchronized (getBlossomActivities()) {
            getBlossomActivities().iterator().forEachRemaining(active -> {
                active.onTick();
                if (!active.getPass()) return;

                getScene().addEntity(active.getChest());
                getScene().setChallenge(null);
                getActiveChests().add(active);
            });
        }
    }

    public void recycleGadgetEntity(List<GameEntity> entities) {
        entities.forEach(entity -> {
            if (!(entity instanceof EntityGadget gadget)) return;

            getCreatedEntity().remove(gadget);
        });
        notifyIcon();
    }

    public void initBlossom(EntityGadget gadget) {
        if (getCreatedEntity().contains(gadget)
            || getBlossomConsumed().contains(gadget.getSpawnEntry())
            ||BlossomType.valueOf(gadget.getGadgetId()) == null) return;

        gadget.buildContent();
        gadget.setState(204);
        GadgetWorktop gadgetWorktop = ((GadgetWorktop) gadget.getContent());
        gadgetWorktop.addWorktopOptions(new int[]{187});
        gadgetWorktop.setOnSelectWorktopOptionEvent((GadgetWorktop context, int option) -> {
            BlossomActivity activity;
            EntityGadget entityGadget = context.getGadget();
            synchronized (getBlossomActivities()) {
                for (BlossomActivity i : getBlossomActivities()) {
                    if (i.getGadget() == entityGadget) {
                        return false;
                    }
                }

                int volume = 0;
                final int fightingVolume = GameDepot.getBlossomConfig().getMonsterFightingVolume();
                List<Integer> monsters = new ArrayList<>();
                while (volume < fightingVolume) {
                    int remain = fightingVolume - volume;

                    int rand = Utils.randomRange(1,100);
                    if (rand > 85 && remain >= 50) { //15% ,generate strong monster
                        monsters.addAll(getRandomMonstersID(2,1));
                        volume += 50;
                    } else if (rand > 50 && remain >= 20) { //35% ,generate normal monster
                        monsters.addAll(getRandomMonstersID(1,1));
                        volume += 20;
                    } else { //50% ,generate weak monster
                        monsters.addAll(getRandomMonstersID(0,1));
                        volume += 10;
                    }
                }

                Grasscutter.getLogger().info("Blossom Monsters: {}", monsters);

                activity = new BlossomActivity(entityGadget, monsters, -1, getWorldLevel());
                getBlossomActivities().add(activity);
            }
            entityGadget.updateState(201);
            getScene().setChallenge(activity.getChallenge());
            getScene().removeEntity(entityGadget, VisionTypeOuterClass.VisionType.VISION_TYPE_REMOVE);
            activity.start();
            return true;
        });
        getCreatedEntity().add(gadget);
        notifyIcon();
    }

    public void notifyIcon() {
        final int worldLevel = Math.max(0, Math.min(getWorldLevel(), 8)); // clip world level [0, 8]
        final var worldLevelData = GameData.getWorldLevelDataMap().get(worldLevel);

        getScene().broadcastPacket(new PacketBlossomBriefInfoNotify(
            GameDepot.getSpawnLists().entrySet().stream().map(entry ->
                entry.getValue().stream()
                    .map(SpawnDataEntry::getGroup)
                    .map(SpawnGroupEntry::getSpawns)
                    .flatMap(List::stream)
                    .filter(spawn -> !getBlossomConsumed().contains(spawn))
                    .filter(spawn -> BlossomType.valueOf(spawn.getGadgetId()) != null)
                    .map(spawn -> {
                        BlossomType type = BlossomType.valueOf(spawn.getGadgetId());
                        return BlossomBriefInfo.newBuilder()
                            .setSceneId(entry.getKey().getSceneId())
                            .setPos(spawn.getPos().toProto())
                            .setResin(20)
                            .setMonsterLevel((worldLevelData != null) ? worldLevelData.getMonsterLevel() : 1)
                            .setRewardId(getPreviewReward(type, worldLevel))
                            .setCircleCampId(type.getCircleCampId())
                            .setRefreshId(type.getBlossomChestId())  // TODO: replace when using actual leylines
                            .build();
                    }).toList()
            ).flatMap(List::stream).toList()
        ));
    }

    public int getWorldLevel() {
        return getScene().getWorld().getWorldLevel();
    }

    private static int getPreviewReward(BlossomType type, int worldLevel) {
        // TODO: blossoms should be based on their city
        return GameData.getBlossomRefreshExcelConfigDataMap().values().stream()
            .filter(data -> type.getBlossomChestId() == data.getBlossomChestId())
            .findFirst()
            .map(data -> {
                val dropVecList = data.getDropVec();
                if (worldLevel > dropVecList.size()) {
                    Grasscutter.getLogger().error("Illegal world level {}", worldLevel);
                    return 0;
                }
                return dropVecList.get(worldLevel).getPreviewReward();
            })
            .orElse(0);
    }

    private static RewardPreviewData getRewardList(BlossomType type, int worldLevel) {
        return GameData.getRewardPreviewDataMap().get(getPreviewReward(type, worldLevel));
    }

    public List<GameItem> onReward(Player player, EntityGadget chest, boolean useCondensedResin) {
        ResinManager resinManager = player.getResinManager();
        synchronized (getActiveChests()) {
            return getActiveChests().stream()
                .filter(activeChest -> activeChest.getChest() == chest)
                .findFirst()
                .map(activeChest -> {
                    boolean payable = useCondensedResin ?
                        resinManager.useCondensedResin(1) :
                        resinManager.useResin(20);

                    if (!payable) return null;
                    EntityGadget gadget = activeChest.getGadget();
                    RewardPreviewData blossomRewards = getRewardList(
                        BlossomType.valueOf(gadget.getGadgetId()), getWorldLevel());

                    return blossomRewards == null ? null : Stream.of(blossomRewards.getPreviewItems())
                        .map(blossomReward -> new GameItem(
                            blossomReward.getItemId(),
                            blossomReward.getCount() * (useCondensedResin ? 2 : 1)))
                        .collect(Collectors.toList());
                })
                .orElse(List.of());
        }
    }

    public static List<Integer> getRandomMonstersID(int difficulty, int count) {
        List<Integer> monsters = GameDepot.getBlossomConfig().getMonsterIdsPerDifficulty().get(difficulty);
        return IntStream.range(0, count)
            .mapToObj(i -> Utils.randomRange(0, monsters.size() - 1))
            .map(monsters::get)
            .toList();
    }
}
