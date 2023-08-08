package emu.grasscutter.game.dungeons.challenge.trigger;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import lombok.Getter;
import lombok.Setter;

public class GuardTrigger extends ChallengeTrigger {
    @Getter @Setter private int lastSendPercent = 100;

    public GuardTrigger(int paramIndex, int entityToProtectCFGId){
        super(paramIndex, entityToProtectCFGId);
    }

    @Override
    public void onBegin(WorldChallenge challenge) {
        Grasscutter.getLogger().info("Percent: {}", getLastSendPercent());
        challenge.getScene().broadcastPacket(new PacketChallengeDataNotify(challenge, getParamIndex(), getLastSendPercent()));
    }

    @Override
    public void onGadgetDamage(WorldChallenge challenge, EntityGadget gadget) {
        if(gadget.getConfigId() != getGoal().get()) return;

        float curHp = gadget.getFightProperties().get(FightProperty.FIGHT_PROP_CUR_HP.getId());
        float maxHp = gadget.getFightProperties().get(FightProperty.FIGHT_PROP_MAX_HP.getId());
        float percent =  (curHp / maxHp) * 100;

        if(percent != getLastSendPercent()) {
            setLastSendPercent((int) percent);
            onBegin(challenge);
        }

        if(percent <= 0) challenge.fail();
    }
}
