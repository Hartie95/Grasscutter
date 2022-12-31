package emu.grasscutter.game.player;

import dev.morphia.annotations.Entity;
import emu.grasscutter.game.props.ClimateType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Entity @Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(builderMethodName = "of")
public class WeatherClimateInfoItem {
    // first integer is area level 1 plus area level 2
    Map<Integer, WeatherClimate> weatherClimate;

    public static WeatherClimateInfoItem create() {
        return WeatherClimateInfoItem.of()
            .weatherClimate(new HashMap<>())
            .build();
    }

    @Entity @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder(builderMethodName = "of")
    public static class WeatherClimate{
        int weatherAreaId;
        int climateType;

        public static WeatherClimate create(int weatherAreaId, ClimateType climate) {
            return WeatherClimate.of()
                .weatherAreaId(weatherAreaId)
                .climateType(climate.getValue())
                .build();
        }

        public synchronized void set(int weatherAreaId, int climate) {
            this.weatherAreaId = weatherAreaId;
            this.climateType = climate;
        }

        public synchronized void reset() {
            this.climateType = 0;
        }
    }
}