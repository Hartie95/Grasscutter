package emu.grasscutter.data.excels;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.game.props.ClimateType;
import lombok.Getter;

@ResourceType(name = "WeatherTemplateExcelConfigData.json") @Getter
public class WeatherTemplateData extends GameResource {
    @Getter private String templateName;
    @Getter private ClimateType weatherType;
    @Getter private float sunnyProb = 0.0f;
    @Getter private float cloudyProb = 0.0f;
    @Getter private float rainProb = 0.0f;
    @Getter private float thunderstormProb = 0.0f;
    @Getter private float snowProb = 0.0f;
    @Getter private float mistProb = 0.0f;

    @Override
    public void onLoad() {
        super.onLoad();
        GameData.getWeatherTemplateDataByNameMap().put(templateName + weatherType.getValue(), this);
    }
}
