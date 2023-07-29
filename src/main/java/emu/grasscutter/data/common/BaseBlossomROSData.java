package emu.grasscutter.data.common;

import emu.grasscutter.game.managers.blossom.enums.BlossomRefreshType;

public interface BaseBlossomROSData {
    // Base Blossom Refresh Or Schedule Data
    int getRefreshId();
    int getCityId();
    int getRewardId(int worldLevel);
    BlossomRefreshType getRefreshType();
}
