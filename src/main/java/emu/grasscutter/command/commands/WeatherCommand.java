package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ClimateType;
import emu.grasscutter.game.world.WeatherArea;

import java.util.List;

@Command(label = "weather", aliases = {"w"}, usage = {"weather [<weatherId>] [<climateType>]"}, permission = "player.weather", permissionTargeted = "player.weather.others")
public final class WeatherCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        int weatherId = targetPlayer.getWeatherId();
        ClimateType climate = ClimateType.CLIMATE_NONE;  // Sending ClimateType.CLIMATE_NONE to Scene.setWeather will use the default climate for that weather

        WeatherArea area = targetPlayer.getScene().getWeatherArea(targetPlayer.getPosition());

        if (args.isEmpty()) {
            if(area != null)
                climate = area.getCurrentClimateType();
            CommandHandler.sendTranslatedMessage(sender, "commands.weather.status", weatherId, climate.getShortName());
            return;
        }

        for (String arg : args) {
            ClimateType c = ClimateType.getTypeByShortName(arg.toLowerCase());
            if (c != ClimateType.CLIMATE_NONE) {
                climate = c;
            } else {
                try {
                    weatherId = Integer.parseInt(arg);
                } catch (NumberFormatException ignored) {
                    CommandHandler.sendTranslatedMessage(sender, "commands.generic.invalid.id");
                    sendUsageMessage(sender);
                    return;
                }
            }
        }

        if(area != null) {
            area.setClimateType(climate);
            climate = area.getCurrentClimateType();
        }
        CommandHandler.sendTranslatedMessage(sender, "commands.weather.success", weatherId, climate.getShortName());
    }
}
