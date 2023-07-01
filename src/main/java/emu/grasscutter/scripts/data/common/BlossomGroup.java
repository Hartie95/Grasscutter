package emu.grasscutter.scripts.data.common;

import emu.grasscutter.scripts.ScriptLoader;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.SceneTrigger;
import lombok.Getter;
import lombok.val;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@CommonScriptType(name = "V2_0/BlossomGroup.lua")
@Getter
public class BlossomGroup extends BaseCommonScript{

    public BlossomGroup(CompiledScript blossomGroup, Bindings blossomGroupBindings) {
        super(blossomGroup, blossomGroupBindings);
    }


    @Override
    public void rebuildTriggersAndBindings(SceneGroup group) {
        // add common script bindings to group bindings
        group.getBindings().putAll(getCommonScriptBindings());
        // get all the config ids
        val allConfigIds = new HashSet<Integer>();
        allConfigIds.addAll(group.monsters.keySet());
        allConfigIds.addAll(group.npcs.keySet());
        allConfigIds.addAll(group.gadgets.keySet());
        allConfigIds.addAll(group.triggers.values().stream().map(SceneTrigger::getConfig_id).toList());
        allConfigIds.addAll(group.regions.keySet());

        // make a copy trigger of common script
        val newTriggers = ScriptLoader.getSerializer().toList(SceneTrigger.class,
                getCommonScriptBindings().get("triggers")).stream()
            .collect(Collectors.toMap(SceneTrigger::getName, y -> y, (a, b) -> a));

        // get the next configIds and put it to group's trigger
        AtomicInteger triggerNextConfigId = new AtomicInteger(Collections.max(allConfigIds) + 1);
        newTriggers.forEach((key, value) -> {
            String newName = value.getName() + "_" + triggerNextConfigId;
            value.setName(newName);
            value.setConfig_id(triggerNextConfigId.getAndIncrement());
            value.setCurrentGroup(group);
            group.triggers.put(newName, value);
        });

        // Currently working for all the blossom group that requires this script
        // TODO check
        group.suites.get(0).triggers.addAll(group.triggers.keySet().stream().toList());
    }

}
