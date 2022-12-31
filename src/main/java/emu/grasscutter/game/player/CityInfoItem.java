package emu.grasscutter.game.player;

import dev.morphia.annotations.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity @Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class CityInfoItem {
    int exp;
    int level;
    // could possibly hold reputation data here later

    public static CityInfoItem create() {
        return CityInfoItem.of()
            .exp(0)
            .level(1)
            .build();
    }

    public void addExp(int exp, int maxExp) {
        this.exp += exp > maxExp ? maxExp : exp;
        if (this.exp < maxExp) return;

        this.exp -= maxExp;
        this.addLevel(1);
    }

    public void addLevel(int level) {
        this.level += level;
    }
}