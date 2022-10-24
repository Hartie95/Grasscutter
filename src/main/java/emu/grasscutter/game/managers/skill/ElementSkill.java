package emu.grasscutter.game.managers.skill;

import emu.grasscutter.GameConstants;

public enum ElementSkill {
    COMMON (501, 701),
    FIRE (502, 702),
    WATER (503, 703),
    WIND (504, 704),
    ICE (505, 705),
    ROCK (506, 706),
    ELECTRO (507, 707),
    GRASS (508, 708);

    private final int boyId;
    private final int girlId;

    ElementSkill(int boyId, int girlId){
        this.boyId = boyId;
        this.girlId = girlId;
    }
    
    public int getSkillByAvatarId(int avatarId){
        return avatarId == GameConstants.MAIN_CHARACTER_MALE ? this.boyId : this.girlId;
    }
}