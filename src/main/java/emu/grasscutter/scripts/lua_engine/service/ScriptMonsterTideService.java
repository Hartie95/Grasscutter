package emu.grasscutter.scripts.lua_engine.service;

import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.scripts.SceneScriptManager;
import emu.grasscutter.scripts.listener.ScriptMonsterListener;
import lombok.val;
import org.anime_game_servers.gi_lua.models.ScriptArgs;
import org.anime_game_servers.gi_lua.models.constants.EventType;
import org.anime_game_servers.gi_lua.models.scene.group.SceneGroup;
import org.anime_game_servers.gi_lua.models.scene.group.SceneMonster;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ScriptMonsterTideService {
    private final SceneScriptManager sceneScriptManager;
    private final int challengeIndex;
    private final SceneGroup currentGroup;
    private final AtomicInteger monsterAlive;
    private final AtomicInteger monsterTideCount;
    private final AtomicInteger monsterKillCount;
    private final int monsterSceneLimit;
    private final ConcurrentLinkedQueue<Integer> monsterConfigOrders;
    private final List<Integer> monsterConfigIds;
    private final OnMonsterCreated onMonsterCreated= new OnMonsterCreated();
    private final OnMonsterDead onMonsterDead= new OnMonsterDead();

    public ScriptMonsterTideService(SceneScriptManager sceneScriptManager, int challengeIndex,
                     SceneGroup group, int tideCount, int monsterSceneLimit, Integer[] ordersConfigId){
        this.sceneScriptManager = sceneScriptManager;
        this.challengeIndex = challengeIndex;
        this.currentGroup = group;
        this.monsterSceneLimit = monsterSceneLimit;
        this.monsterTideCount = new AtomicInteger(tideCount);
        this.monsterKillCount = new AtomicInteger(0);
        this.monsterAlive = new AtomicInteger(0);
        this.monsterConfigOrders = new ConcurrentLinkedQueue<>(List.of(ordersConfigId));
        this.monsterConfigIds = List.of(ordersConfigId);

        this.sceneScriptManager.getScriptMonsterSpawnService().addMonsterCreatedListener(onMonsterCreated);
        this.sceneScriptManager.getScriptMonsterSpawnService().addMonsterDeadListener(onMonsterDead);
        // spawn the first turn
        for (int i = 0; i < this.monsterSceneLimit; i++) {
            sceneScriptManager.addEntity(this.sceneScriptManager.createMonster(getNextMonster()));
        }
    }

    public class OnMonsterCreated implements ScriptMonsterListener{
        @Override
        public void onNotify(EntityMonster sceneMonster) {
            if(monsterConfigIds.contains(sceneMonster.getConfigId()) && monsterSceneLimit > 0){
                monsterAlive.incrementAndGet();
                monsterTideCount.decrementAndGet();
            }
        }
    }

    public SceneMonster getNextMonster(){
        val nextId = this.monsterConfigOrders.poll();
        val monsters = currentGroup.getMonsters();
        if (monsters == null) {
            return null;
        }
        if(monsters.containsKey(nextId)){
            return monsters.get(nextId);
        }
        // TODO some monster config_id do not exist in groups, so temporarily set it to the first
        return monsters.values().stream().findFirst().orElse(null);
    }

    public class OnMonsterDead implements ScriptMonsterListener {
        @Override
        public void onNotify(EntityMonster sceneMonster) {
            if (monsterSceneLimit <= 0) {
                return;
            }
            if (monsterAlive.decrementAndGet() >= monsterSceneLimit) {
                // maybe not happen
                return;
            }
            monsterKillCount.incrementAndGet();
            if (monsterTideCount.get() > 0) {
                // add more
                sceneScriptManager.addEntity(sceneScriptManager.createMonster(getNextMonster()));
            }
            // spawn the last turn of monsters
            // fix the 5-2
            sceneScriptManager.callEvent(new ScriptArgs(currentGroup.getGroupInfo().getId(), EventType.EVENT_MONSTER_TIDE_DIE, monsterKillCount.get())
                .setEventSource(String.valueOf(challengeIndex)));
        }

    }

    public void unload(){
        this.sceneScriptManager.getScriptMonsterSpawnService().removeMonsterCreatedListener(onMonsterCreated);
        this.sceneScriptManager.getScriptMonsterSpawnService().removeMonsterDeadListener(onMonsterDead);
    }
}
