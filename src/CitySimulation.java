import city_gen_model.CityParamType;
import city_gen_model.CityState;
import city_gen_model.CityStateParameters;
import city_gen_model.ProgressionModel;
import city_gen_model.city_events.MapEventBroadcaster;
import city_gen_model.city_events.MapEventType;
import model.events.EventBroadcaster;

import city_gen_model.city_events.MapEvent;
import processing.core.PApplet;
import utils.ConfigUtils;
import view.components.Button;
import view.components.CustomProcedure;
import view.drawer.InfoDrawer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CitySimulation extends PApplet {

    int width = 1000;
    int height = 500;
    InfoDrawer infoDrawer;

    List<Button> buttons;

    CityState cityState;
    MapEventBroadcaster eventBroadcaster;

    public void settings() {
        size(width, height);
    }

    public void setup() {
        infoDrawer = new InfoDrawer(this);
        eventBroadcaster = new MapEventBroadcaster();

        String cityStateParamsConfig = "src/configs/city_configs/city_state.json";
        try {
            cityState = new CityState(eventBroadcaster, ConfigUtils.readCityStateParameters(cityStateParamsConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttons = new ArrayList<>();

        int i = 0;
        for (MapEventType mapEventType : MapEventType.values()) {
            buttons.add(new Button(mapEventType.toString(),
                    width-180, 20+i*30, 170, 25, this,
                    new CustomProcedure() {
                        @Override
                        public void proc() { eventBroadcaster.broadcastEvent(
                                new MapEvent(mapEventType,
                                        0, 0, 0, 50, 500)); }
                    }));
            i++;
        }

        buttons.add(new Button("Next",
                width-180, height-80, 170, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() {
                        cityState.update();
                    }
                }));
    }

    public void draw() {
        background(255);
        for (Button b : buttons) {
            b.update();
        }
        for (Button b : buttons) {
            b.display();
        }

        int i = 0;
        for (CityParamType cityParamType : CityParamType.values()) {
            infoDrawer.drawTextBox(cityParamType + ": " + cityState.getCityStateParameters().getQuantity(cityParamType), 20, 20 * i +20, 150);
            i++;
        }
    }

    public static void main(String... args){
        PApplet.main("CitySimulation");
    }
}
