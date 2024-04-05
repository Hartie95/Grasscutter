package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.world.WeatherArea.WeatherRefreshType;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.TypedPacketHandler;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketSceneWeatherForcastRsp;
import lombok.val;
import messages.scene.weather.SceneWeatherForecastReq;

@Opcodes(PacketOpcodes.SceneWeatherForcastReq)
public class HandlerSceneWeatherForcastReq extends TypedPacketHandler<SceneWeatherForecastReq> {

    @Override
    public void handle(GameSession session, byte[] header, SceneWeatherForecastReq req) throws Exception {
        val areaId = req.getWeatherAreaId();
        val player = session.getPlayer();
        val area = player.getScene().getWeatherAreas().get(areaId);
        if(area != null) {
            area.refresh(WeatherRefreshType.REFRESH_WEATHER_FORCAST);
            val forcast = area.getForcastList();
            session.getPlayer().sendPacket(new PacketSceneWeatherForcastRsp(forcast));
        } else {
            session.getPlayer().sendPacket(new PacketSceneWeatherForcastRsp(Retcode.RET_FAIL_VALUE));
        }
    }

}
