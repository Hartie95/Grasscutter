package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.WeatherClimateInfoItem.WeatherClimate;
import emu.grasscutter.game.world.World;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.SceneAreaWeatherNotifyOuterClass.SceneAreaWeatherNotify;

public class PacketSceneAreaWeatherNotify extends BasePacket {
	
	// public PacketSceneAreaWeatherNotify(Player player) {
	// 	super(PacketOpcodes.SceneAreaWeatherNotify);
		
	// 	SceneAreaWeatherNotify proto = SceneAreaWeatherNotify.newBuilder()
	// 			.setWeatherAreaId(player.getWeatherId())
	// 			.setClimateType(player.getClimate().getValue())
	// 			.build();
		
	// 	this.setData(proto);
	// }

	public PacketSceneAreaWeatherNotify(Player player) {
		super(PacketOpcodes.SceneAreaWeatherNotify);
		SceneAreaWeatherNotify proto = SceneAreaWeatherNotify.newBuilder()
				.setWeatherAreaId(player.getCurrentWeatherClimate().getWeatherAreaId())
				.setClimateType(player.getCurrentWeatherClimate().getClimateType())
				.build();
		
		this.setData(proto);
	}

	public PacketSceneAreaWeatherNotify(int weatherAreaId, int climateValue) {
		super(PacketOpcodes.SceneAreaWeatherNotify);
		SceneAreaWeatherNotify proto = SceneAreaWeatherNotify.newBuilder()
				.setWeatherAreaId(weatherAreaId)
				.setClimateType(climateValue)
				.build();
		
		this.setData(proto);
	}
}
