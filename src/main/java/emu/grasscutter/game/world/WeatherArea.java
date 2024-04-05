package emu.grasscutter.game.world;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.WeatherData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.Player.SceneLoadState;
import emu.grasscutter.game.props.ClimateType;
import emu.grasscutter.server.packet.send.PacketSceneAreaWeatherNotify;
import lombok.Getter;
import lombok.val;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mongodb.lang.Nullable;

public class WeatherArea {
    @Getter private final Scene scene;
    @Getter private WeatherData config;
    @Getter private ClimateType climateType;
    @Getter private float transDuration = 0;
    @Nullable
    private ClimateType currentClimateType = null; //TODO: Related to abilities

    public static float WeatherChangeInterval = 4;
    public static int WeatherForcastNum = 0xC;
    public static int GameHourSeconds = 10000;

    @Getter private int currentTime = 0;
    @Getter private final List<Integer> forcastList = new ArrayList<>();

    @Getter private final List<Player> players = new CopyOnWriteArrayList<>();

    public enum WeatherRefreshType {
        REFRESH_WEATHER_NONE(0),
        REFRESH_WEATHER_HOUR(1),
        REFRESH_WEATHER_ENTER(2),
        REFRESH_WEATHER_QUERY(3),
        REFRESH_WEATHER_FORCAST(4),
        REFRESH_WEATHER_INIT(5)
        ;

        private final int value;

        WeatherRefreshType(int id) {
            this.value = id;
        }

        public int getValue() {
            return value;
        }
    }

    public WeatherArea(Scene scene, WeatherData config) {
        this.scene = scene;
        this.config = config;
    }

    public ClimateType getCurrentClimateType() {
        if(currentClimateType == null)
            return climateType;
        return currentClimateType;
    }

    public void setClimateType(ClimateType type) {
        this.scene.getSceneInstanceData().updateWeather(config.getAreaID(), type);
        this.climateType = type;
    }

    public void init() {
        refresh(WeatherRefreshType.REFRESH_WEATHER_INIT);
    }

    public boolean enterArea(Player player) {
        if(players.contains(player)) return false;
        players.add(player);

        refresh(WeatherRefreshType.REFRESH_WEATHER_ENTER);

        return true;
    }

    public boolean leaveArea(Player player) {
        if(!players.contains(player)) return false;

        players.remove(player);
        return true;
    }

    public void remove() {
        for (Player player : this.players) {
            //Remove player from this area and notify it
            if(player.getSceneLoadState().getValue() >= SceneLoadState.INIT.getValue())
                player.sendPacket(new PacketSceneAreaWeatherNotify(0, ClimateType.CLIMATE_NONE, 0));
        }
    }

    public ClimateType randWeather(ClimateType initClimateType) {
        val mapping = GameData.getWeatherMappingMap().get(config.getAreaID());
        if(mapping == null) return ClimateType.CLIMATE_SUNNY;

        var searchType = climateType;
        if(searchType == ClimateType.CLIMATE_NONE) searchType = config.getDefaultClimate();
        val templateData = GameData.getWeatherTemplateDataByNameMap().get(mapping.getTemplate()+searchType.getValue());
        if(templateData == null) return ClimateType.CLIMATE_SUNNY;

        float maxNumber = templateData.getSunnyProb() +
                          templateData.getCloudyProb() +
                          templateData.getRainProb() +
                          templateData.getThunderstormProb() +
                          templateData.getSnowProb() +
                          templateData.getMistProb();
        if(maxNumber == 0.0) return config.getDefaultClimate();
        float randomNumber = (float)Math.random() * maxNumber;
        if(randomNumber < templateData.getSunnyProb())
            return ClimateType.CLIMATE_SUNNY;
        else if(randomNumber < templateData.getCloudyProb())
            return ClimateType.CLIMATE_CLOUDY;
        else if(randomNumber < templateData.getRainProb())
            return ClimateType.CLIMATE_RAIN;
        else if(randomNumber < templateData.getThunderstormProb())
            return ClimateType.CLIMATE_THUNDERSTORM;
        else if(randomNumber < templateData.getSnowProb())
            return ClimateType.CLIMATE_SNOW;
        else if(randomNumber < templateData.getMistProb())
            return ClimateType.CLIMATE_MIST;

        return ClimateType.CLIMATE_SUNNY;
    }

    public ClimateType randWeather() {
        return randWeather(config.getDefaultClimate());
    }

    public void refresh(WeatherRefreshType type) {
        if(config.isDefaultValid()) {
            //Easy part

            setClimateType(config.getDefaultClimate());
        } else {
            if(scene.getWorld().isWeatherLocked()) return;

            int refreshInterval = (int)(WeatherChangeInterval * GameHourSeconds);

            ClimateType oldClimateType = climateType;

            //here do nothing if weather is locked
            int sceneTime = scene.getSceneTimeSeconds();
            int nextTime = sceneTime + (refreshInterval - sceneTime % refreshInterval);
            if(currentTime <= sceneTime) {
                if(/*forcastList.isEmpty() || */(nextTime <= currentTime)) return;

                ClimateType cType = ClimateType.CLIMATE_NONE;
                if(currentTime < nextTime) {
                    if(currentTime == 0) {
                        cType = config.getDefaultClimate();
                    } else {
                        cType = randWeather();
                    }

                    setClimateType(cType);
                    currentTime = nextTime;
                }
                for(int i = forcastList.size(); i < WeatherForcastNum; i++) {
                    int forcastId = 0;
                    if(forcastList.isEmpty()) {
                        forcastId = climateType.getValue();
                    } else {
                        forcastId = forcastList.get(forcastList.size() - 1);
                    }
                    int newForcastId = randWeather(ClimateType.getTypeByValue(forcastId)).getValue();
                    forcastList.add(newForcastId);
                }
                if(oldClimateType != climateType) {
                    if(type == WeatherRefreshType.REFRESH_WEATHER_HOUR) {
                        for (Player player : this.players) {
                            if(player.getSceneLoadState().getValue() >= SceneLoadState.INIT.getValue())
                                player.sendPacket(new PacketSceneAreaWeatherNotify(this.config.getAreaID(), climateType, transDuration));
                        }
                    }
                    if(type == WeatherRefreshType.REFRESH_WEATHER_NONE ||
                       type == WeatherRefreshType.REFRESH_WEATHER_HOUR ||
                       type == WeatherRefreshType.REFRESH_WEATHER_ENTER) {
                        //TODO: scene.onWeatherChange
                        //TODO: envanimal -> VISION_GATHER_ESCAPE related
                    }
                }
            }
            if(nextTime < currentTime) {
                //Rollback
                currentTime = nextTime;
            }
        }
    }
}
