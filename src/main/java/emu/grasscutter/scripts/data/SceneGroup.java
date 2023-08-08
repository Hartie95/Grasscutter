package emu.grasscutter.scripts.data;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.world.GroupReplacementData;
import emu.grasscutter.scripts.ScriptLoader;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.script.Bindings;
import javax.script.CompiledScript;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ToString
@Setter
public class SceneGroup {
    public transient int block_id; // Not an actual variable in the scripts but we will keep it here for reference

    @Accessors(chain = true) public int id;
    public int refresh_id;
    public Position pos;

    public Map<Integer,SceneMonster> monsters; // <ConfigId, Monster>
    public Map<Integer, SceneNPC> npcs; // <ConfigId, Npc>
    public Map<Integer, SceneGadget> gadgets; // <ConfigId, Gadgets>
    public Map<String, SceneTrigger> triggers;
    public Map<Integer, SceneRegion> regions;
    public List<SceneSuite> suites;
    public List<SceneVar> variables;

    public SceneBusiness business;
    public SceneGarbage garbages;
    public SceneInitConfig init_config;
    @Getter public boolean dynamic_load = false;

    public SceneReplaceable is_replaceable;

    private transient boolean loaded; // Not an actual variable in the scripts either
    private transient CompiledScript script;
    private transient Bindings bindings;
    public static SceneGroup of(int groupId) {
        return new SceneGroup().setId(groupId);
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public int getBusinessType() {
        return this.business == null ? 0 : this.business.type;
    }

    public List<SceneGadget> getGarbageGadgets() {
        return this.garbages == null ? null : this.garbages.gadgets;
    }

    public CompiledScript getScript() {
        return this.script;
    }

    public SceneSuite getSuiteByIndex(int index) {
        return index < 1 || index > this.suites.size() ? null : this.suites.get(index - 1);
    }

    public Bindings getBindings() {
        return this.bindings;
    }

    public synchronized SceneGroup load(int sceneId) {
        if (this.loaded) return this;
        // Set flag here so if there is no script, we don't call this function over and over again.
        this.setLoaded(true);
        this.bindings = ScriptLoader.getEngine().createBindings();
        CompiledScript cs = ScriptLoader.getScript("Scene/" + sceneId + "/scene" + sceneId + "_group" + this.id + ".lua");
        if (cs == null) return this;

        this.script = cs;
        // Eval script
        try {
            cs.eval(this.bindings);

            // Set
            this.monsters = ScriptLoader.getSerializer().toList(SceneMonster.class, this.bindings.get("monsters")).stream()
                    .collect(Collectors.toMap(x -> x.config_id, y -> y, (a, b) -> a));
            this.monsters.values().forEach(m -> m.group = this);

            this.npcs = ScriptLoader.getSerializer().toList(SceneNPC.class, this.bindings.get("npcs")).stream()
                    .collect(Collectors.toMap(x -> x.config_id, y -> y, (a, b) -> a));
            this.npcs.values().forEach(m -> m.group = this);

            this.gadgets = ScriptLoader.getSerializer().toList(SceneGadget.class, this.bindings.get("gadgets")).stream()
                    .collect(Collectors.toMap(x -> x.config_id, y -> y, (a, b) -> a));
            this.gadgets.values().forEach(m -> m.group = this);

            this.triggers = ScriptLoader.getSerializer().toList(SceneTrigger.class, this.bindings.get("triggers")).stream()
                    .collect(Collectors.toMap(SceneTrigger::getName, y -> y, (a, b) -> a));
            this.triggers.values().forEach(t -> t.currentGroup = this);

            this.regions = ScriptLoader.getSerializer().toList(SceneRegion.class, this.bindings.get("regions")).stream()
                .collect(Collectors.toMap(x -> x.config_id, y -> y, (a, b) -> a));
            this.regions.values().forEach(m -> m.group = this);

            this.init_config = ScriptLoader.getSerializer().toObject(SceneInitConfig.class, this.bindings.get("init_config"));

            // Garbages
            this.garbages = ScriptLoader.getSerializer().toObject(SceneGarbage.class, this.bindings.get("garbages"));
            Optional.ofNullable(this.garbages.gadgets).ifPresent(g -> g.forEach(m -> m.group = this));

            // Add variables to suite
            this.variables = ScriptLoader.getSerializer().toList(SceneVar.class, this.bindings.get("variables"));

            // Add monsters and gadgets to suite
            this.suites = ScriptLoader.getSerializer().toList(SceneSuite.class, this.bindings.get("suites"));
            this.suites.forEach(i -> i.init(this));
        } catch (Exception e) {
            Grasscutter.getLogger().error("An error occurred while loading group " + this.id + " in scene " + sceneId + ".", e);
        }

        Grasscutter.getLogger().debug("Successfully loaded group {} in scene {}.", this.id, sceneId);
        return this;
    }

    public int findInitSuiteIndex(int exclude_index) { // TODO: Investigate end index
        if(this.init_config == null) return 1;
        if(this.init_config.io_type == 1 || !this.init_config.rand_suite || this.suites.size() == 1) return this.init_config.suite; // IO TYPE FLOW
        if (exclude_index >= this.suites.size()) return 1;

        return Utils.drawRandomListElement(
            IntStream.rangeClosed(1, suites.size()).filter(i -> i != exclude_index).boxed().toList(),
            IntStream.rangeClosed(1, suites.size()).filter(i -> i != exclude_index)
                .mapToObj(i -> this.suites.get(i-1)).map(s -> s.rand_weight).toList());
    }

    public Optional<SceneBossChest> searchBossChestInGroup() {
        return this.gadgets.values().stream()
            .filter(g -> g.boss_chest != null && g.boss_chest.monster_config_id > 0)
            .map(g -> g.boss_chest)
            .findFirst();
    }

    public List<SceneGroup> getReplaceableGroups(Collection<SceneGroup> loadedGroups){
        return this.is_replaceable == null ? List.of() :
        Optional.ofNullable(GameData.getGroupReplacements().get(this.id)).stream()
            .map(GroupReplacementData::getReplace_groups)
            .flatMap(List::stream)
            .map(replacementId -> loadedGroups.stream().filter(g -> g.id == replacementId).findFirst())
            .filter(Optional::isPresent).map(Optional::get)
            .filter(replacementGroup -> replacementGroup.is_replaceable != null)
            .filter(replacementGroup -> (replacementGroup.is_replaceable.value
                && replacementGroup.is_replaceable.version <= this.is_replaceable.version)
                || replacementGroup.is_replaceable.new_bin_only)
            .toList();
    }
}
