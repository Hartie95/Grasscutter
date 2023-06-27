package emu.grasscutter.game.managers.blossom.enums;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum BlossomRefreshType {
    BLOSSOM_REFRESH_NONE(0, 0),
    BLOSSOM_REFRESH_SCOIN(1, 70360056),
    BLOSSOM_REFRESH_EXP(3, 70360057);
//    BLOSSOM_REFRESH_INFUSED_CRYSTAL(3, -1); // not sure
    // followings are not sure, giving value so that it does not affect the working ones
//    BLOSSOM_REFRESH_DRAGON_SPINE_B(4, 70560218),
//    BLOSSOM_REFRESH_DRAGON_SPINE_A(5, 70560218),
//    BLOSSOM_REFRESH_CRYSTAL(6, -1),
//    BLOSSOM_REFRESH_BLITZ_RUSH_A(7, -1),
//    BLOSSOM_REFRESH_BLITZ_RUSH_B(8, -1),
//    BLOSSOM_ISLAND_SENTRY_TOWER_A(9, -1),
//    BLOSSOM_ISLAND_SENTRY_TOWER_B(10, -1),
//    BLOSSOM_ISLAND_BOMB(11, -1);

    private final int value;
    private final int gadgetId; // not chest gadget id
    private static final Int2ObjectMap<BlossomRefreshType> map = new Int2ObjectOpenHashMap<>();
    private static final Map<String, BlossomRefreshType> stringMap = new HashMap<>();

    static {
        Stream.of(values()).forEach(e -> {
            map.put(e.getValue(), e);
            stringMap.put(e.name(), e);
        });
    }

    BlossomRefreshType(int value, int gadgetId) {
        this.value = value;
        this.gadgetId = gadgetId;
    }

    public int getValue() {
        return value;
    }

    public int getGadgetId() {
        return this.gadgetId;
    }

    public static BlossomRefreshType getTypeByValue(int value) {
        return map.getOrDefault(value, BLOSSOM_REFRESH_NONE);
    }

    public static BlossomRefreshType getTypeByName(String name) {
        return stringMap.getOrDefault(name, BLOSSOM_REFRESH_NONE);
    }
}
