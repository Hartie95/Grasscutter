package emu.grasscutter.scripts.data.common;

import emu.grasscutter.scripts.data.SceneGroup;
import lombok.Getter;

import javax.script.Bindings;
import javax.script.CompiledScript;

public abstract class BaseCommonScript {
    /**
     * Used to hold Lua Script related stuff
     * */
    @Getter
    private final transient CompiledScript commonScript;
    /**
     * Used to hold Lua Script related stuff
     * */
    @Getter private final transient Bindings commonScriptBindings;

    public BaseCommonScript(CompiledScript commonScript, Bindings commonScriptBindings) {
        this.commonScript = commonScript;
        this.commonScriptBindings = commonScriptBindings;
    }

    /**
     * Used to rebuild triggers and bindings for all the subsequent common scripts
     * */
    public abstract void rebuildTriggersAndBindings(SceneGroup group);
}
