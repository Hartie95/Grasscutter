package emu.grasscutter.game.entity;

import emu.grasscutter.data.binout.config.ConfigEntityGadget;
import emu.grasscutter.data.binout.config.fields.ConfigAbilityData;
import emu.grasscutter.data.excels.GadgetData;
import emu.grasscutter.game.ability.AbilityManager;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.entity.gadget.content.GadgetContent;
import emu.grasscutter.game.entity.interfaces.ConfigAbilityDataAbilityEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.server.event.entity.EntityDamageEvent;
import emu.grasscutter.server.packet.send.PacketGadgetStateNotify;
import emu.grasscutter.utils.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.anime_game_servers.gi_lua.models.ScriptArgs;
import org.anime_game_servers.gi_lua.models.constants.EventType;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetInteractReq;
import org.anime_game_servers.multi_proto.gi.messages.scene.VisionType;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.anime_game_servers.gi_lua.models.constants.EventType.EVENT_SPECIFIC_GADGET_HP_CHANGE;

public abstract class EntityBaseGadget extends GameEntity<CreateGadgetEntityConfig> implements ConfigAbilityDataAbilityEntity {
    @Getter protected final int gadgetId;

    @Getter private final Position bornPos;
    @Getter private final Position bornRot;
    @Getter(onMethod = @__(@Override))
    protected final Position position;
    @Getter(onMethod = @__(@Override))
    protected final Position rotation;

    @Getter private final int campId;
    @Getter private final int campType;
    @Getter private int state;
    @Getter private int ticksSinceStateChange;
    @Nullable @Getter private final GadgetData gadgetData;
    @Nullable @Getter private final ConfigEntityGadget configGadget;
    @Getter private final GadgetContent content;
    @Getter @Setter private boolean interactEnabled;
    @Getter private int interactId;
    @Getter @Setter private List<Integer> arguments;
    @Getter @Setter private Player owner;
    @Getter @Setter private GameEntity<?> ownerEntity;
    @Getter @Setter private int ownerEntityId;
    @Getter @Setter private List<GameEntity<?>> children = new ArrayList<>();

    protected EntityBaseGadget(Scene scene, CreateGadgetEntityConfig gadgetCreateConfig) {
        super(scene, gadgetCreateConfig);
        this.gadgetId = gadgetCreateConfig.getGadgetId();
        this.bornPos = gadgetCreateConfig.getBornPos();
        this.bornRot = gadgetCreateConfig.getBornRot();
        this.position = gadgetCreateConfig.getPos();
        this.rotation = gadgetCreateConfig.getRot();
        this.campId = gadgetCreateConfig.getCampId();
        this.campType = gadgetCreateConfig.getCampType();
        this.state = gadgetCreateConfig.getGadgetState();
        this.gadgetData = gadgetCreateConfig.getGadgetData();
        this.configGadget = gadgetCreateConfig.getConfigEntityGadget();
        this.interactEnabled = gadgetCreateConfig.isEnableInteract();
        this.interactId = gadgetCreateConfig.getInteractId();
        this.arguments = gadgetCreateConfig.getArguments();
        this.owner = gadgetCreateConfig.getPlayerOwner();

        if(gadgetCreateConfig.getContent() == null) {
            this.content = buildContent(gadgetCreateConfig);
        } else {
            this.content = gadgetCreateConfig.getContent();
        }

        fillFightProps(configGadget);
        initAbilities();
    }

    public abstract GadgetContent buildContent(CreateGadgetEntityConfig config);

    @Override
    public int getEntityTypeId() {
        return getGadgetId();
    }

    @Override
    public void onDeath(int killerId) {
        super.onDeath(killerId); // Invoke super class's onDeath() method.

        getScene().getPlayers().forEach(p -> p.getQuestManager().queueEvent(QuestContent.QUEST_CONTENT_DESTROY_GADGET, this.getGadgetId()));
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if(!children.isEmpty()) {
            getScene().removeEntities(children, VisionType.VISION_REMOVE);
            children.clear();
        }
    }

    @Override
    public void callLuaHPEvent(EntityDamageEvent event) {
        super.callLuaHPEvent(event);
        getScene().getScriptManager().callEvent(new ScriptArgs(this.getGroupId(), EVENT_SPECIFIC_GADGET_HP_CHANGE, getConfigId(), getGadgetId())
            .setSourceEntityId(getId())
            .setParam3((int) this.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP))
            .setEventSource(getConfigId())
        );
    }

    protected void fillFightProps(ConfigEntityGadget configGadget) {
        if (configGadget == null || configGadget.getCombat() == null || getFightProperties() == null) {
            return;
        }
        var combatData = configGadget.getCombat();
        var combatProperties = combatData.getProperty();

        var targetHp = combatProperties.getHP();
        setFightProperty(FightProperty.FIGHT_PROP_MAX_HP, targetHp);
        setFightProperty(FightProperty.FIGHT_PROP_BASE_HP, targetHp);
        if (combatProperties.isInvincible()) {
            targetHp = Float.POSITIVE_INFINITY;
        }
        setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, targetHp);

        var atk = combatProperties.getAttack();
        setFightProperty(FightProperty.FIGHT_PROP_BASE_ATTACK, atk);
        setFightProperty(FightProperty.FIGHT_PROP_CUR_ATTACK, atk);

        var def = combatProperties.getDefence();
        setFightProperty(FightProperty.FIGHT_PROP_BASE_DEFENSE, def);
        setFightProperty(FightProperty.FIGHT_PROP_CUR_DEFENSE, def);

        setLockHP(combatProperties.isLockHP());
    }

    /*
     * Ability Related
     */

    //TODO: handle predynamic, static and dynamic here
    @Override
    public Collection<ConfigAbilityData> getAbilityData() {
        return this.configGadget != null ? this.configGadget.getAbilities() : null;
    }

    @Override
    public AbilityManager getAbilityTargetManager() {
        return getWorld().getHost().getAbilityManager();
    }

    @Override
    public void onInteract(Player player, GadgetInteractReq interactReq) {
        if(!isInteractEnabled()) return;

        if (this.getContent() == null) {
            return;
        }

        boolean shouldDelete = this.getContent().onInteract(player, interactReq);

        if (shouldDelete) {
            this.getScene().killEntity(this);
        }
    }

    public void setState(int state) {
        this.state = state;
    }

    public void updateState(int state) {
        if(state == this.getState()) return; //Don't triggers events

        val oldState = this.getState();
        this.setState(state);
        ticksSinceStateChange = getScene().getSceneTimeSeconds();
        this.getScene().broadcastPacket(new PacketGadgetStateNotify(this, state));
        getScene().getScriptManager().callEvent(new ScriptArgs(this.getGroupId(), EventType.EVENT_GADGET_STATE_CHANGE)
            .setParam1(state)
            .setParam2(this.getConfigId())
            .setParam3(oldState)
            .setSourceEntityId(getId()));
    }
}
