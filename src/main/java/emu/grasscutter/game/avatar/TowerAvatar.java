package emu.grasscutter.game.avatar;

import dev.morphia.annotations.Entity;
import emu.grasscutter.game.entity.EntityWeapon;
import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.inventory.EquipType;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.TowerItem;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.server.packet.send.PacketAvatarEquipChangeNotify;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.val;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity @Getter
public class TowerAvatar extends Avatar {
    private final Map<Integer, TowerItem> towerItemMap = new Int2ObjectOpenHashMap<>();
    private final Map<Integer, Float> towerFightProperties = new Int2FloatOpenHashMap();
    private final Map<Integer, Float> towerFightPropOverrides = new Int2FloatOpenHashMap();
    private final Set<String> towerExtraAbilityEmbryos = new HashSet<>();
    private final Map<Integer, Integer> towerSkillExtraChargeMap = new Int2IntArrayMap();
    private final Map<Integer, Integer> towerProudSkillBonusMap = new Int2IntArrayMap();

    public TowerAvatar(Avatar original) {
        super(original.getAvatarId());
        this.level = original.level;
        this.promoteLevel = original.promoteLevel;
        this.fetters.addAll(original.fetters);
        this.talentIdList.addAll(original.talentIdList);
        this.proudSkillList.addAll(original.proudSkillList);
        this.skillLevelMap.putAll(original.skillLevelMap);

        if (original instanceof TowerAvatar towerAvatar) {
            this.towerItemMap.putAll(towerAvatar.towerItemMap);

            this.equips.putAll(towerAvatar.towerItemMap);
            this.fightProperties.putAll(towerAvatar.towerFightProperties);
            this.fightPropOverrides.putAll(towerAvatar.towerFightPropOverrides);
            this.extraAbilityEmbryos.addAll(towerAvatar.towerExtraAbilityEmbryos);
            this.skillExtraChargeMap.putAll(towerAvatar.towerSkillExtraChargeMap);
            this.proudSkillBonusMap.putAll(towerAvatar.towerProudSkillBonusMap);

            this.currentHp = original.currentHp;
            this.currentEnergy = original.currentEnergy;
            this.guid = original.guid;
        } else {
            this.data = original.data;

            this.equips.clear();
            this.equips.putAll(original.equips.values().stream().map(TowerItem::new).collect(
                Collectors.toMap(GameItem::getEquipSlot, item -> item)));
            this.fightProperties.clear(); this.fightProperties.putAll(original.fightProperties);
            this.fightPropOverrides.clear(); this.fightPropOverrides.putAll(original.fightPropOverrides);
            this.extraAbilityEmbryos.clear(); this.extraAbilityEmbryos.addAll(original.extraAbilityEmbryos);
            this.skillExtraChargeMap.clear(); this.skillExtraChargeMap.putAll(original.skillExtraChargeMap);
            this.proudSkillBonusMap.clear(); this.proudSkillBonusMap.putAll(original.proudSkillBonusMap);
            setSkillDepot(original.skillDepot);

            this.currentHp = getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
            val energySkillData = this.skillDepot.getEnergySkillData();
            this.currentEnergy = energySkillData!=null ? energySkillData.getCostElemVal() : 0;

            // put to save
            this.equips.forEach((k, v) -> this.towerItemMap.put(k, new TowerItem(v)));
        }
        this.towerFightProperties.putAll(this.fightProperties);
        this.towerFightPropOverrides.putAll(this.fightPropOverrides);
        this.towerExtraAbilityEmbryos.addAll(this.extraAbilityEmbryos);
        this.towerSkillExtraChargeMap.putAll(this.skillExtraChargeMap);
        this.towerProudSkillBonusMap.putAll(this.proudSkillBonusMap);
        setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, this.currentHp);
        setFightProperty(this.skillDepot.getElementType().getCurEnergyProp(), this.currentEnergy);
    }

    public void equipItems() {
        this.equips.forEach((itemEquipTypeValues, item) -> {
            item.setOwner(getPlayer());
            if (item.getItemData().getEquipType() == EquipType.EQUIP_WEAPON && getPlayer().getWorld() != null) {
                if(!(item.getWeaponEntity() != null && item.getWeaponEntity().getScene() == getPlayer().getScene())) {
                    val weaponConfig = new CreateGadgetEntityConfig(item.getItemData().getGadgetId());
                    item.setWeaponEntity(new EntityWeapon(getPlayer().getScene(), weaponConfig));
                    getPlayer().getScene().getWeaponEntities().put(item.getWeaponEntity().getId(), item.getWeaponEntity());
                }
                getPlayer().sendPacket(new PacketAvatarEquipChangeNotify(this, item));
            }
        });
        recalcStats();
    }

    public TowerAvatar copy() {
        return new TowerAvatar(this);
    }

    @Override
    public void save() {

    }

    @Override
    protected void prePersist() {

    }
}
