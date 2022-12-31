package emu.grasscutter.data.binout;

import emu.grasscutter.utils.Position;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class SceneWorldArea {
    private List<LevelAreaData> level1Areas;

    @Getter 
    public class LevelAreaData {
        private AreaData level1Area;
        private List<AreaData> level2Areas;
    }

    @Getter 
    public class AreaData {
        private int id2;
        private int id1;
        private PolygonData polygonData;
    }

    @Getter 
    public class PolygonData {
        private Position minArea;
        private Position maxArea;
        // roadpoints
    }
}