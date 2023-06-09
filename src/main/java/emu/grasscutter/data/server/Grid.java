package emu.grasscutter.data.server;

import java.util.*;
import java.util.Map.Entry;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.scripts.SceneIndexManager;
import emu.grasscutter.utils.GridPosition;
import emu.grasscutter.utils.Position;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Geometry;
import lombok.val;

public class Grid {
    public Map<GridPosition, Set<Integer>> grid;

    public transient RTree<Entry<GridPosition, Set<Integer>>, Geometry> gridOptimized = null;

    private transient HashSet<Integer> nearbyGroups = new HashSet<>(100);

    private void Optimize() {
        if(gridOptimized == null) {
            List<Entry<GridPosition, Set<Integer>>> gridValues = new ArrayList<>();
            grid.forEach((k, v) -> {
                gridValues.add(new AbstractMap.SimpleEntry<>(k, v));
            });
            gridOptimized = SceneIndexManager.buildIndex(2, gridValues, entry -> entry.getKey().toPoint());
        }

    }

    public Set<Integer> getNearbyGroups(int vision_level, Position position) {
        Optimize();

        int width = Grasscutter.getConfig().server.game.visionOptions[vision_level].gridWidth;
        int vision_range = Grasscutter.getConfig().server.game.visionOptions[vision_level].visionRange;
        int vision_range_grid = vision_range / width;

        GridPosition pos = new GridPosition(position, width);

        nearbyGroups.clear();
        //construct a nearby position list, add 1 more because a player can be in an edge case, this should not affect much the loading
        //for(int x = 0; x < vision_range_grid + 1; x++) {
        //    for(int z = 0; z < vision_range_grid + 1; z++) {
        //        //nearbyGroups.addAll(grid.getOrDefault(pos.addClone( x,  z), new HashSet<>()));
        //        //nearbyGroups.addAll(grid.getOrDefault(pos.addClone(-x,  z), new HashSet<>()));
        //        //nearbyGroups.addAll(grid.getOrDefault(pos.addClone( x, -z), new HashSet<>()));
        //        //nearbyGroups.addAll(grid.getOrDefault(pos.addClone(-x, -z), new HashSet<>()));
        //    }
        //}

        //Optimized version
        SceneIndexManager.queryNeighbors(gridOptimized, pos.toDoubleArray(), vision_range_grid + 1).forEach(e -> nearbyGroups.addAll(e.getValue()));

        return nearbyGroups;
    }
}
