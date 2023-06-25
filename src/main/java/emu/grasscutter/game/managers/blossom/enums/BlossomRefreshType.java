package emu.grasscutter.game.managers.blossom.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum BlossomRefreshType {
    BLOSSOM_REFRESH_NONE,
    BLOSSOM_REFRESH_SCOIN,
    BLOSSOM_REFRESH_EXP,
    BLOSSOM_REFRESH_CRYSTAL,
    BLOSSOM_REFRESH_INFUSED_CRYSTAL,
    BLOSSOM_REFRESH_DRAGON_SPINE_A,
    BLOSSOM_REFRESH_DRAGON_SPINE_B,
    BLOSSOM_ISLAND_SENTRY_TOWER_A,
    BLOSSOM_ISLAND_SENTRY_TOWER_B,
    BLOSSOM_ISLAND_BOMB,
    BLOSSOM_REFRESH_BLITZ_RUSH_A,
    BLOSSOM_REFRESH_BLITZ_RUSH_B;

    private static final Map<String, BlossomRefreshType> stringMap = new HashMap<>();

    static {
        Stream.of(values()).forEach(e -> stringMap.put(e.name(), e));
    }

    public static BlossomRefreshType getTypeByName(String name) {
        return stringMap.getOrDefault(name, BLOSSOM_REFRESH_NONE);
    }
}
