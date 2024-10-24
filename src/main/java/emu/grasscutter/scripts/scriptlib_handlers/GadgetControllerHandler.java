package emu.grasscutter.scripts.scriptlib_handlers;

import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.scripts.SceneScriptManager;
import emu.grasscutter.scripts.lua_engine.ControllerLuaContext;
import lombok.Getter;
import lombok.val;
import org.anime_game_servers.gi_lua.script_lib.handler.parameter.KillByConfigIdParams;
import org.anime_game_servers.lua.engine.LuaTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class GadgetControllerHandler extends BaseHandler implements org.anime_game_servers.gi_lua.script_lib.handler.GadgetControllerHandler<EntityGadget, ControllerLuaContext> {
    @Getter
    private static final Logger logger = BaseHandler.getLogger();

    /**
     * Methods used in EntityControllers/using ControllerLuaContext
     */

    @Override
    public int SetGadgetState(ControllerLuaContext context, int gadgetState) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        gadget.updateState(gadgetState);
        return 0;
    }

    @Override
    public int GetGadgetState(ControllerLuaContext context) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        return gadget.getState();
    }

    @Override
    public int ResetGadgetState(ControllerLuaContext context, int gadgetState) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        gadget.getPosition().set(gadget.getBornPos());
        gadget.getRotation().set(gadget.getBornRot());
        gadget.setStartValue(0);
        gadget.setStopValue(0);
        gadget.updateState(gadgetState);
        return 0;
    }

    @Override
    public int SetGearStartValue(ControllerLuaContext context, int startValue) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        gadget.setStartValue(startValue);
        return 0;
    }

    @Override
    public int GetGearStartValue(ControllerLuaContext context) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        return gadget.getStartValue();
    }

    @Override
    public int SetGearStopValue(ControllerLuaContext context, int startValue) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        gadget.setStopValue(startValue);
        return 0;
    }

    @Override
    public int GetGearStopValue(ControllerLuaContext context) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        return gadget.getStopValue();
    }

    @Override
    public int GetGadgetStateBeginTime(ControllerLuaContext context) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        return gadget.getTicksSinceStateChange();
    }

    @Override
    public int GetContextGadgetConfigId(ControllerLuaContext context) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        return gadget.getConfigId();
    }

    @Override
    public int GetContextGroupId(ControllerLuaContext context) {
        EntityGadget gadget = context.getEntity();
        if(gadget == null) return -1;

        return gadget.getGroupId();
    }

    @Override
    public int SetGadgetEnableInteract(ControllerLuaContext context, int groupId, int configId, boolean enable) {
        val gadget = context.getEntity();
        if(gadget.getGroupId() != groupId || gadget.getConfigId() != configId) return -1;

        gadget.setInteractEnabled(enable);

        return 0;
    }

    @Override
    public int DropSubfield(ControllerLuaContext context, LuaTable params) {
        val gadget = context.getEntity();
        String subfield_name = params.getString("subfield_name");

        gadget.dropSubfield(subfield_name);

        return -1;
    }

    @Override
    public int[] GetGatherConfigIdList(ControllerLuaContext context) {
        val gadget = context.getEntity();
        val children = gadget.getChildren();

        val configIds = new int[children.size()];
        for(int i = 0; i < children.size(); i++) {
            configIds[i] = children.get(i).getConfigId();
        }

        return configIds;
    }


    @Override
    public int KillEntityByConfigId(@NotNull ControllerLuaContext controllerLuaContext, @NotNull KillByConfigIdParams killByConfigIdParams) {
        logger.debug("[LUA] Call KillEntityByConfigId with {}", killByConfigIdParams);
        SceneScriptManager scriptManager = controllerLuaContext.getGadget().getScene().getScriptManager();

        int groupId = killByConfigIdParams.getGroupId() != 0 ? killByConfigIdParams.getGroupId() : controllerLuaContext.getGadget().getGroupId();
        var entity = scriptManager.getScene().getEntityByConfigId(killByConfigIdParams.getConfigId(), groupId);
        if (entity == null) {
            return 0;
        }
        scriptManager.getScene().killEntity(entity, 0);
        return 0;
    }

    @NotNull
    @Override
    public int[] GetGadgetArguments(@NotNull ControllerLuaContext controllerLuaContext) {
        val gadgetArguments = controllerLuaContext.getGadget().getMetaGadget().getArguments();
        return gadgetArguments != null ? gadgetArguments.stream().mapToInt(Integer::intValue).toArray() : new int[0];
    }
}
