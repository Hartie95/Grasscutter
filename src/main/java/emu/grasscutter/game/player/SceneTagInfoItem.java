package emu.grasscutter.game.player;

import dev.morphia.annotations.Entity;
import emu.grasscutter.data.GameData;
import emu.grasscutter.net.proto.PlayerWorldSceneInfoOuterClass.PlayerWorldSceneInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

@Entity @Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class SceneTagInfoItem {
    int sceneId;
    Set<Integer> tags;
    boolean isLocked;

    public static SceneTagInfoItem create(int sceneId, Collection<Integer> sceneTag, boolean isLocked) {
        return SceneTagInfoItem.of()
            .sceneId(sceneId)
            .tags(new TreeSet<Integer>(sceneTag))
            .isLocked(isLocked)
            .build();
    }

    public void addTag(String tagId) {
        addTag(Integer.parseInt(tagId));
    }

    public void addTag(int tagId) {
        tags.add(tagId);
    }

    public void removeTag(String tagId) {
        tags.remove(Integer.valueOf(tagId));
    }

    public void unlock() {
        isLocked = false;
    }

    public void lock() {
        isLocked = true;
    }

    public PlayerWorldSceneInfo toProto() {
        return PlayerWorldSceneInfo.newBuilder()
            .setSceneId(sceneId)
            .addAllSceneTagIdList(tags)
            .setIsLocked(isLocked)
            .build();
    }
}