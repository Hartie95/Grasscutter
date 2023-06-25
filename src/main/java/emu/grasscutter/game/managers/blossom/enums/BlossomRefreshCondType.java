package emu.grasscutter.game.managers.blossom.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum BlossomRefreshCondType {
    BLOSSOM_REFRESH_COND_NONE,
    BLOSSOM_REFRESH_COND_PLAYER_LEVEL_EQUAL_GREATER,
    BLOSSOM_REFRESH_COND_PLAYER_LEVEL_LESS_THAN,
    BLOSSOM_REFRESH_COND_OPEN_STATE,
    BLOSSOM_REFRESH_COND_ACTIVITY_COND,
    BLOSSOM_REFRESH_COND_SCENE_TAG_ADDED;

    private static final Map<String, BlossomRefreshCondType> stringMap = new HashMap<>();

    static {
        Stream.of(values()).forEach(e -> stringMap.put(e.name(), e));
    }

    public static BlossomRefreshCondType getTypeByName(String name) {
        return stringMap.getOrDefault(name, BLOSSOM_REFRESH_COND_NONE);
    }
}
