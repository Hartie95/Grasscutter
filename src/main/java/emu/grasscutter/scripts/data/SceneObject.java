package emu.grasscutter.scripts.data;

import emu.grasscutter.utils.Position;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "group")
@Setter
public class SceneObject {
    public int level;
    public int config_id;
    public int area_id;
    public int vision_level = 0;
    public int mark_flag;
    public String drop_tag;

    public Position pos;
    public Position rot;
    /**
     * not set by lua
     */
    public transient SceneGroup group;
}
