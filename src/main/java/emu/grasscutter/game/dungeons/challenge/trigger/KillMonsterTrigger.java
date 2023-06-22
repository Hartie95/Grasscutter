package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityMonster;
import lombok.Getter;

public class KillMonsterTrigger extends ChallengeTrigger{
    @Getter private final int monsterCfgId;

    public KillMonsterTrigger(int monsterCfgId){
        super(0);
        this.monsterCfgId = monsterCfgId;
    }

    @Override
    public void onMonsterDeath(WorldChallenge challenge, EntityMonster monster) {
        if(monster.getConfigId() == getMonsterCfgId()){
            challenge.done();
        }
    }
}
