package emu.grasscutter.game.avatar;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.custom.TrialAvatarCustomData;
import emu.grasscutter.data.excels.AvatarCostumeData;
import emu.grasscutter.data.excels.TrialAvatarTemplateData;
import emu.grasscutter.data.excels.TrialReliquaryData;
import emu.grasscutter.game.inventory.EquipType;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.net.proto.AvatarInfoOuterClass;
import emu.grasscutter.net.proto.TrialAvatarGrantRecordOuterClass.TrialAvatarGrantRecord;
import emu.grasscutter.net.proto.TrialAvatarGrantRecordOuterClass.TrialAvatarGrantRecord.GrantReason;
import emu.grasscutter.net.proto.TrialAvatarInfoOuterClass.TrialAvatarInfo;
import emu.grasscutter.server.packet.send.PacketAvatarEquipChangeNotify;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TrialAvatar extends Avatar{
    // trial avatar property
    @Getter @Setter private int trialAvatarId = 0;
    // cannot store to db if grant reason is not integer
    @Getter @Setter private int grantReason = GrantReason.GRANT_REASON_INVALID.getNumber();
    @Getter @Setter private int fromParentQuestId = 0;
    // so far no outer class or prop value has information of this, but from packet I sniff
    // 1 = normal, 2 = trial avatar
    @Getter private final int avatarType = 2;

    public TrialAvatar(List<Integer> trialAvatarParam, int trialAvatarId, GrantReason grantReason, int fromParentQuestId) {
        super(trialAvatarParam.get(0));
        this.setLevel(trialAvatarParam.get(1));
        this.setPromoteLevel(getMinPromoteLevel(trialAvatarParam.get(1)));
        this.setTrialAvatarId(trialAvatarId);
        this.setGrantReason(grantReason.getNumber());
        this.setFromParentQuestId(fromParentQuestId);
        this.setTrialSkillLevel();
        this.setTrialItems();
    }

    private int getTrialAvatarTemplateLevel(){
        return getLevel() <= 9 ? 1 :
            (int) (Math.floor(getLevel() / 10f) * 10); // round trial level to fit template levels
    }

    public int getTrialSkillLevel() {
        val trialCustomData = GameData.getTrialAvatarCustomData();
        if (trialCustomData.isEmpty()) { // use default data if custom data not available
            int trialAvatarTemplateLevel = getTrialAvatarTemplateLevel(); // round trial level to fit template levels

            TrialAvatarTemplateData templateData = GameData.getTrialAvatarTemplateDataMap().get(trialAvatarTemplateLevel);
            return templateData == null ? 1 : templateData.getTrialAvatarSkillLevel();
        }

        TrialAvatarCustomData trialAvatarCustomData= trialCustomData.get(getTrialAvatarId());
        if (trialAvatarCustomData == null) return 1;

        return trialAvatarCustomData.getCoreProudSkillLevel(); // enhanced version of weapon
    }

    public void setTrialSkillLevel() {
        getSkillLevelMap().keySet().forEach(skill -> setSkillLevel(skill, getTrialSkillLevel()));
    }

    public int getTrialWeaponId() {
        val trialCustomData = GameData.getTrialAvatarCustomData();
        int initialWeapon = getAvatarData().getInitialWeapon();

        if (trialCustomData.isEmpty()) { // use default data if custom data not available
            if (GameData.getTrialAvatarDataMap().get(getTrialAvatarId()) == null
                || GameData.getItemDataMap().get(initialWeapon+100) == null)
                return initialWeapon;

            return initialWeapon+100; // enhanced version of weapon
        }

        // use custom data
        TrialAvatarCustomData trialAvatarCustomData= trialCustomData.get(getTrialAvatarId());
        if (trialAvatarCustomData == null) return 0;

        val trialCustomParams = trialAvatarCustomData.getTrialAvatarParamList();
        return trialCustomParams.size() < 2 ? initialWeapon :
            Integer.parseInt(trialCustomParams.get(1).split(";")[0]);
    }

    public List<Integer> getTrialReliquary() {
        if (GameData.getTrialAvatarCustomData().isEmpty()) {
            // try using custom data
            if (GameData.getTrialAvatarCustomData().get(getTrialAvatarId()) != null) {
                val trialCustomParams = GameData.getTrialAvatarCustomData().get(getTrialAvatarId()).getTrialAvatarParamList();
                if (trialCustomParams.size() > 2) {
                    return Stream.of(trialCustomParams.get(2).split(";")).map(Integer::parseInt).toList();
                }
            }
        }
        int trialAvatarTemplateLevel = getTrialAvatarTemplateLevel();

        TrialAvatarTemplateData templateData = GameData.getTrialAvatarTemplateDataMap().get(trialAvatarTemplateLevel);
        return templateData == null ? List.of() : templateData.getTrialReliquaryList();
    }

    public void setTrialItems(){
        // add enhanced version of trial weapon
        GameItem weapon = new GameItem(getTrialWeaponId());
        weapon.setLevel(getLevel());
        weapon.setExp(0);
        weapon.setPromoteLevel(getMinPromoteLevel(getLevel()));
        getEquips().put(weapon.getEquipSlot(), weapon);

        // add Trial Artifacts
        getTrialReliquary().forEach(id -> {
            TrialReliquaryData reliquaryData = GameData.getTrialReliquaryDataMap().get(id);
            if (reliquaryData == null) return;

            GameItem relic = new GameItem(reliquaryData.getReliquaryId());
            relic.setLevel(reliquaryData.getLevel());
            relic.setMainPropId(reliquaryData.getMainPropId());
            relic.getAppendPropIdList().addAll(reliquaryData.getAppendPropList());
            getEquips().put(relic.getEquipSlot(), relic);
        });

        // add costume if any (Amber, Rosaria, Mona, Jean)
        this.setCostume(GameData.getAvatarCostumeDataItemIdMap().values()
            .stream().map(AvatarCostumeData::getCharacterId)
            .filter(characterId -> characterId == this.getAvatarId())
            .findAny().orElse(0));
    }

    public void equipTrialItems(){
        getEquips().forEach((itemEquipTypeValues, item) -> {
            item.setEquipCharacter(getAvatarId());
            item.setOwner(getPlayer());
            if (item.getItemData().getEquipType() == EquipType.EQUIP_WEAPON && getPlayer().getWorld() != null) {
                item.setWeaponEntityId(this.getPlayer().getWorld().getNextEntityId(EntityIdType.WEAPON));
                getPlayer().sendPacket(new PacketAvatarEquipChangeNotify(this, item));
            }
        });
    }

    public TrialAvatarInfo trialAvatarInfoProto(){
        TrialAvatarInfo.Builder trialAvatar = TrialAvatarInfo.newBuilder()
            .setTrialAvatarId(this.getTrialAvatarId())
            .setGrantRecord(TrialAvatarGrantRecord.newBuilder()
                .setGrantReason(this.getGrantReason())
                .setFromParentQuestId(this.getFromParentQuestId()));

        if (this.getTrialAvatarId() > 0){ // if it is actual trial avatar
            // add artifacts and weapon for trial character
            AtomicInteger itemCount = new AtomicInteger();
            this.getEquips().values().forEach(item ->
                trialAvatar.addTrialEquipList(itemCount.getAndIncrement(), item.toProto()));
        }

        return trialAvatar.build();
    }

    @Override
    public AvatarInfoOuterClass.AvatarInfo.Builder protoBuilder() {
        return super.protoBuilder()
            .setAvatarType(getAvatarType())
            .setTrialAvatarInfo(this.trialAvatarInfoProto());
    }

    @Override
    public void save() {
        // Don't need to save any of the trial avatar
    }
}
