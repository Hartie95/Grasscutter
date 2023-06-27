package emu.grasscutter.scripts.data.common;

import emu.grasscutter.game.entity.GameEntity;
import lombok.Getter;
import org.luaj.vm2.LuaValue;

import javax.script.Bindings;
import javax.script.CompiledScript;

@CommonScriptType(name = "BlossomGroupV2.lua")
@Getter
public class BlossomGroupV2 extends BaseCommonScript{

    public BlossomGroupV2(CompiledScript blossomGroup, Bindings blossomGroupBindings) {
        super(blossomGroup, blossomGroupBindings);
    }

    public void onBlossomChestDie(GameEntity entity) {
        super.callCommonScriptFunc(entity, "onBlossomChestDie",
            LuaValue.valueOf(0), LuaValue.valueOf(0), LuaValue.valueOf(0));
    }

    public void createReward(GameEntity entity, int groupId) {
        LuaValue result = super.callCommonScriptFunc(entity, "createReward",
            LuaValue.valueOf(groupId), LuaValue.valueOf(0), LuaValue.valueOf(0));
        if (result.toint() != 0) return;

        onBlossomProgressFinish(entity);
    }

    public void onBlossomProgressFinish(GameEntity entity) {
        super.callCommonScriptFunc(entity, "onBlossomProgressFinish",
            LuaValue.valueOf(0), LuaValue.valueOf(0), LuaValue.valueOf(0));
    }

    public void createNextMonsterWave(GameEntity entity) {
        int groupId = entity.getGroupId();
        super.callCommonScriptFunc(entity, "createNextMonsterWave",
            LuaValue.valueOf(entity.getScene().getScriptManager().getGroupById(groupId).suites.size()),
            LuaValue.valueOf(groupId), LuaValue.valueOf(0));
    }

    public LuaValue startMonsterWave(GameEntity entity) {
        return super.callCommonScriptFunc(entity, "startMonsterWave",
            LuaValue.valueOf(entity.getGroupId()), LuaValue.valueOf(0), LuaValue.valueOf(0));
    }

    public void onSelectOption(GameEntity entity) {
        LuaValue result = super.callCommonScriptFunc(entity, "onSelectOption",
            LuaValue.valueOf(entity.getGroupId()), LuaValue.valueOf(entity.getConfigId()), LuaValue.valueOf(0));
        if (result.toint() != 0) return;

        result = startMonsterWave(entity);
        if (result.toint() != 0) return;

        createNextMonsterWave(entity);
    }
}
