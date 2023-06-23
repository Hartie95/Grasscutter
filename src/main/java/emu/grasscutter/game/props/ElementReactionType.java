package emu.grasscutter.game.props;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public enum ElementReactionType {
    None(0),
    Explode(1),
    Stream(2),
    Burning(3),
    Burned(4),
    Wet(5),
    GrassGrowing(6),
    Melt(7),
    Freeze(8),
    AntiFire(9),
    Rock(10),
    SlowDown(11),
    Shock(12),
    Wind(13),
    Electric(14),
    Fire(15),
    Superconductor(16),
    SwirlFire(17),
    SwirlWater(18),
    SwirlElectric(19),
    SwirlIce(20),
    SwirlFireAccu(21),
    SwirlWaterAccu(22),
    SwirlElectricAccu(23),
    SwirlIceAccu(24),
    StickRock(25),
    StickWater(26),
    CrystalliseFire(27),
    CrystalliseWater(28),
    CrystalliseElectric(29),
    CrystalliseIce(30);

    private final int value;
    private static final Int2ObjectMap<ElementReactionType> map = new Int2ObjectOpenHashMap<>();
    private static final Map<String, ElementReactionType> stringMap = new HashMap<>();

    static {
        java.util.stream.Stream.of(values()).forEach(e -> {
            map.put(e.getValue(), e);
            stringMap.put(e.name(), e);
        });
    }

    private ElementReactionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ElementReactionType getTypeByValue(int value) {
        return map.getOrDefault(value, None);
    }

    public static ElementReactionType getTypeByName(String name) {
        return stringMap.getOrDefault(name, None);
    }

}
