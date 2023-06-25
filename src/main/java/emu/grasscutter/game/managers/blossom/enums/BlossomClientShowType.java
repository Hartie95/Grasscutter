package emu.grasscutter.game.managers.blossom.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum BlossomClientShowType {
    BLOSSOM_SHOWTYPE_NONE,
    BLOSSOM_SHOWTYPE_CHALLENGE,
    BLOSSOM_SHOWTYPE_NPCTALK,
    BLOSSOM_SHOWTYPE_GROUPCHALLENGE;

    private static final Map<String, BlossomClientShowType> stringMap = new HashMap<>();

    static {
        Stream.of(values()).forEach(e -> stringMap.put(e.name(), e));
    }

    public static BlossomClientShowType getTypeByName(String name) {
        return stringMap.getOrDefault(name, BLOSSOM_SHOWTYPE_NONE);
    }
}
