package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.*;
import emu.grasscutter.server.game.GameSession;
import messages.scene.LevelupCityReq;

public class HandlerLevelupCityReq extends TypedPacketHandler<LevelupCityReq> {

    @Override
    public void handle(GameSession session, byte[] header, LevelupCityReq req) throws Exception {
        // Level up city
        session
                .getPlayer()
                .getSotsManager()
                .levelUpSotS(req.getAreaId(), req.getSceneId(), req.getItemNum());
    }
}