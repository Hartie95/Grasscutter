package emu.grasscutter.scripts.data;

import lombok.*;

@Data
@NoArgsConstructor
// todo find way to deserialize from lua with final fields, maybe with the help of Builder?
public class SceneTrigger {
	private String name;
    private int config_id;
    private int event;
    private int trigger_count = 1;
    private String source;
    private String condition;
    private String action;
    private String tag;
    private String tlog_tag;
    public boolean forbid_guest;

	public transient SceneGroup currentGroup;

    /**
     * To prevent unlimited recursion when logging
     * */
    public String toString(){
        return "Name: " + this.name + ", ConfigId: " + this.config_id + ", Event: " + this.event
            + ", Condition: " + this.condition + ", Action: " + this.action;
    }
}
