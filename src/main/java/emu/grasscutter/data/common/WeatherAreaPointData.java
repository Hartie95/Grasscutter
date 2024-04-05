package emu.grasscutter.data.common;

import emu.grasscutter.utils.Position;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings(value = "SpellCheckingInspection")
public class WeatherAreaPointData {
    public int area_id;
    public boolean isAccurateBorder = false;
    public PolygonPoint[] points;

    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuppressWarnings(value = "SpellCheckingInspection")
    public class PolygonPoint {
        public float x;
        public float y;
    }

    public boolean isInside(Position position) {
        float posX = position.getX();
        float posZ = position.getZ();
        boolean result = false;
        for(int i = 0, j = points.length - 1; i < points.length; j = i++) {
            if((points[i].y > posZ) != (points[j].y > posZ) &&
                (posX < (points[j].x - points[i].x) * (posZ - points[i].y) / (points[j].y-points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }
}
