package emu.grasscutter.scripts.data.common;

import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.scripts.ScriptLib;
import emu.grasscutter.scripts.ScriptLoader;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.Getter;
import lombok.val;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

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
     * Call common Lua scripts with arguments
     * @param entity current entity
     * @param funcName Lua function to run
     * @param arg1 optional parameter 1
     * @param arg2 optional parameter 2
     * @param arg3 optional parameter 3
     * */
    protected LuaValue callCommonScriptFunc(GameEntity entity, String funcName, LuaValue arg1, LuaValue arg2, LuaValue arg3) {
        LuaValue funcLua = (funcName == null || funcName.isEmpty()) ? null :
            (LuaValue) getCommonScriptBindings().get(funcName);

        LuaValue ret = LuaValue.ONE;

        if (funcLua != null) {
            try {
                val sceneScriptManager = entity.getScene().getScriptManager();
                if (sceneScriptManager == null) {
                    ScriptLib.logger.error("[LUA] call function '{}' failed in gadget {} with {} {} {}.Scene script manager is null",
                        funcName, entity.getEntityTypeId(), arg1, arg2, arg3);
                    return ret;
                }
                SceneGroup currentGroup = sceneScriptManager.getGroupById(entity.getGroupId());
                if (currentGroup == null) {
                    ScriptLib.logger.error("[LUA] call function '{}' failed in gadget {} with {} {} {}. No group with this id.",
                        funcName, entity.getEntityTypeId(), arg1, arg2, arg3);
                    return ret;
                }
                ScriptLoader.getScriptLib().setCurrentEntity(entity);
                ScriptLoader.getScriptLib().setSceneScriptManager(sceneScriptManager);
                ScriptLoader.getScriptLib().setCurrentGroup(currentGroup);
                ret = funcLua.invoke(new LuaValue[]{ScriptLoader.getScriptLibLua(), arg1, arg2, arg3}).arg1();
            } catch (LuaError error) {
                ScriptLib.logger.error("[LUA] call function '{}' failed in gadget {} with {} {} {},{}",
                    funcName, entity.getEntityTypeId(), arg1, arg2, arg3, error);
                ret = LuaValue.valueOf(-1);
            } finally {
                ScriptLoader.getScriptLib().removeCurrentEntity();
                ScriptLoader.getScriptLib().removeSceneScriptManager();
                ScriptLoader.getScriptLib().removeCurrentGroup();
            }
        } else if (funcName != null) {
            ScriptLib.logger.error("[LUA] unknown func '{}' in gadget {} with {} {} {}",
                funcName, entity.getEntityTypeId(), arg1, arg2, arg3);
        }
        return ret;
    }
}
