package emu.grasscutter.scripts.constants;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum GadgetType implements IntValueEnum{
    GADGET_NONE(0),
    GADGET_WORLD_CHECT(1),
    GADGET_DUNGEON_SECRET_CHEST(2),
    GADGET_DUNGEON_PASS_CHEST(3);

    private final int value;
    private static final Int2ObjectMap<GadgetType> map = new Int2ObjectOpenHashMap<>();
    private static final Map<String, GadgetType> stringMap = new HashMap<>();

    static {
        Stream.of(values()).forEach(e -> {
            map.put(e.getValue(), e);
            stringMap.put(e.name(), e);
        });
    }

    GadgetType(int type) {
        this.value = type;
    }

    @Override
    public int getValue() {
        return this.value;
    }
    public static GadgetType getTypeByValue(int value) {
        return map.getOrDefault(value, GADGET_NONE);
    }

    public static GadgetType getTypeByName(String name) {
        return stringMap.getOrDefault(name, GADGET_NONE);
    }
}
