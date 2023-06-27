package emu.grasscutter.data.binout;

import com.google.gson.annotations.SerializedName;
import emu.grasscutter.utils.JsonUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class ScriptSceneData {
    Map<String,ScriptObject> scriptObjectList;

    @Data
    public static class ScriptObject {
        //private SceneGroup groups;
        @SerializedName("dummy_points")
        private Map<String, List<Float>> dummyPoints;

        private Object gadgets;
        private Object groups;
        private Object suites;

        public void onLoad() {
            if (this.gadgets instanceof List) {
                this.gadgets = ((List<?>) this.gadgets).stream()
                    .map(g -> JsonUtils.decode(JsonUtils.encode(g), SceneGadgetData.class))
                    .filter(Objects::nonNull)
                    .toList();
                return;
            }
            this.gadgets = List.of();

            if (this.groups instanceof List) {
                this.groups = ((List<?>) this.groups).stream()
                    .map(g -> JsonUtils.decode(JsonUtils.encode(g), SceneBlockData.class))
                    .filter(Objects::nonNull)
                    .toList();
                return;
            }
            this.groups = List.of();

            if (this.suites instanceof List) {
                this.suites = ((List<?>) this.suites).stream()
                    .map(g -> JsonUtils.decode(JsonUtils.encode(g), SceneSuiteData.class))
                    .filter(Objects::nonNull)
                    .filter(s -> s.getMonsters() != null)
                    .toList();
                return;
            }
            this.suites = List.of();
        }

        @Data
        public static class SceneGadgetData {
            List<Float> pos;
            @SerializedName("area_id")
            private int areaId;
            @SerializedName("config_id")
            private int configId;
            @SerializedName("gadget_id")
            private int gadgetId;
            private int level;
            List<Float> rot;
            @SerializedName("is_blossom_chest")
            private boolean blossomChest;

            public String toString() {
                return "pos: (" + getPos() + "), rot: (" + getRot() +
                    "), areaId: " + getAreaId() + ", configId: " + getConfigId() + ", gadgetId: " + getGadgetId();
            }
        }

        @Data
        public static class SceneBlockData {
            private int id;
            private List<Float> pos;
            private int area;
            public String toString() {
                return "Id: " + getId() + ", pos: (" + getPos() + "), areaId: " + getArea();
            }
        }

        @Data
        public static class SceneSuiteData {
            private List<Integer> monsters;
            public String toString() {
                return "MonsterIds: " + getMonsters();
            }
        }
    }
}
