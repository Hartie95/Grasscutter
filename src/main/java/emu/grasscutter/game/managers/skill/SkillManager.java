package emu.grasscutter.game.managers.skill;

import emu.grasscutter.data.excels.AvatarSkillDepotData;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ElementType;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.packet.send.PacketAvatarChangeElementTypeRsp;
import emu.grasscutter.server.packet.send.PacketAbilityChangeNotify;
import emu.grasscutter.server.packet.send.PacketAvatarSkillChangeNotify;
import emu.grasscutter.server.packet.send.PacketAvatarSkillDepotChangeNotify;
import emu.grasscutter.server.packet.send.PacketAvatarFightPropNotify;


public class SkillManager {

    private static ElementSkill getElementSkillFromType(ElementType element){
        return switch (element) {
            case Fire -> ElementSkill.FIRE;
            case Water -> ElementSkill.WATER;
            case Wind -> ElementSkill.WIND;
            case Ice -> ElementSkill.ICE;
            case Rock -> ElementSkill.ROCK;
            case Electric -> ElementSkill.ELECTRO;
            case Grass -> ElementSkill.GRASS;
            default -> ElementSkill.COMMON;
        };
    }

    public static boolean changeAvatarElement (Player player, int skillId){
        EntityAvatar mainCharacterEntity = player.getTeamManager().getCurrentAvatarEntity();
        Avatar mainCharacter = mainCharacterEntity.getAvatar();
        int nextSkillId = skillId;
        if (skillId < 10){
            ElementType nextElementType = ElementType.getTypeByValue(skillId);
            ElementSkill nextElementSkill = getElementSkillFromType(nextElementType);
            nextSkillId = nextElementSkill.getSkillByAvatarId(mainCharacter.getAvatarId());
        }
        // Sanity checks for skill depots
        AvatarSkillDepotData skillDepot = GameData.getAvatarSkillDepotDataMap().get(nextSkillId);
        if (skillDepot == null || skillDepot.getId() == mainCharacter.getSkillDepotId()) {
            return false;
        }

        // Set skill depot
        mainCharacter.setSkillDepotData(skillDepot);

        // Ability change packet
        player.getSession().send(new PacketAvatarSkillDepotChangeNotify(mainCharacter));
        player.getSession().send(new PacketAbilityChangeNotify(mainCharacterEntity));
        return true;
    }
}