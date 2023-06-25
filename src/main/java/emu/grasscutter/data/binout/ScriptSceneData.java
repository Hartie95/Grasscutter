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

        private Object groups;

        public void onLoad() {
            if (this.groups instanceof List) {
                this.groups = ((List<?>) this.groups).stream()
                    .map(g -> JsonUtils.decode(JsonUtils.encode(g), SceneBlockData.class))
                    .filter(Objects::nonNull)
                    .toList();
                return;
            }
            this.groups = List.of();
        }
    }

    @Data
    public static class SceneBlockData{
        private int id;
        private List<Float> pos;
        private int area;
//        @SerializedName("business.type")
//        private int businessType;
//        @SerializedName("dynamic_load")
//        private boolean dynamicLoad;
//        @SerializedName("is_replaceable.new_bin_only")
//        private boolean replaceableNewBinOnly;
//        @SerializedName("is_replaceable.value")
//        private boolean replaceableValue;
//        @SerializedName("is_replaceable.version")
//        private int replaceableVersion;
        @SerializedName("refresh_id")
        private int refreshId;
//        @SerializedName("vision_type")
//        private int visionType;
//        @SerializedName("activity_revise_level_grow_id")
//        private int activityReviseLevelGrowId;
//        @SerializedName("life_cycle")
//        private List<List<List<LifeCycleData>>> lifeCycle;
//
//        @Data
//        public static class LifeCycleData{
//            private String direction;
//            private int param;
//        }
    }

}
