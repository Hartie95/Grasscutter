package emu.grasscutter.game.player;

import dev.morphia.annotations.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity @Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class DungeonInfoItem {
    int dungeonId;
    boolean isPassed;

    public static DungeonInfoItem create(int dungeonId) {
        return DungeonInfoItem.of()
            .dungeonId(dungeonId)
            .isPassed(false)
            .build();
    }

    public void finish() {
        isPassed = true;
    }
}