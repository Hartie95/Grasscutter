package emu.grasscutter.game.world;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ClimateType;
import lombok.Getter;

@Entity(value = "scene_datas", useDiscriminator = false)
public class SceneInstanceData {
    @Id private ObjectId id;

    @Indexed private int ownerUid;
    @Getter private int sceneId;

    @Getter private Map<Integer, Integer> weatherAreas;

    public SceneInstanceData(Scene scene, Player owner) {
        this.sceneId = scene.getId();
        this.ownerUid = owner.getUid();

        this.weatherAreas = new ConcurrentHashMap<>();
    }

    @Deprecated  // Morphia only!
    SceneInstanceData(){
        this.weatherAreas = new ConcurrentHashMap<>();
    }

    public void save() {
        DatabaseHelper.saveSceneInstanceData(this);
    }

    public void addWeather(int areaId) {
        if(!weatherAreas.containsKey(areaId))
            weatherAreas.put(areaId, ClimateType.CLIMATE_NONE.getValue());
    }

    public void updateWeather(int areaId, ClimateType type) {
        if(weatherAreas.containsKey(areaId))
            weatherAreas.replace(areaId, type.getValue());
    }

    public void removeWeather(int areaId) {
        if(weatherAreas.containsKey(areaId))
            weatherAreas.remove(areaId);
    }
}
