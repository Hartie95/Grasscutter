package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import messages.scene.CityInfo;
import messages.scene.LevelupCityRsp;

public class PacketLevelupCityRsp extends BaseTypedPacket<LevelupCityRsp> {

    public PacketLevelupCityRsp(
            int sceneId, int level, int cityId, int crystalNum, int areaId, int retcode) {
        super(new LevelupCityRsp());

        proto.setSceneId(sceneId);
        proto.setCityInfo(new CityInfo(cityId, crystalNum, level));
        proto.setAreaId(areaId);
        proto.setRetcode(retcode);
    }
}