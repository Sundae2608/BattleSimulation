import city_gen_model.CityState;
import city_gen_model.ProgressionModel;
import city_gen_model.settings.CitySimulationSettings;
import model.events.EventBroadcaster;
import model.events.EventType;
import model.events.MapEvent;
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
    ProgressionModel progressionModel;
    EventBroadcaster eventBroadcaster;

    CitySimulationSettings citySimulationSettings;

    public void settings() {
        size(width, height);

        citySimulationSettings = new CitySimulationSettings();
        citySimulationSettings.setTimeDelaySec(0.25);
    }

    public void setup() {
        infoDrawer = new InfoDrawer(this);
        eventBroadcaster = new EventBroadcaster();

        String cityStateParamsConfig = "src/configs/city_configs/city_state.json";
        try {
            cityState = new CityState(eventBroadcaster, ConfigUtils.readCityStateParameters(cityStateParamsConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }

        progressionModel = new ProgressionModel();
        buttons = new ArrayList<>();
        buttons.add(new Button("Trigger Decay Event",
                width-180, height-50, 170, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { eventBroadcaster.broadcastEvent(
                            new MapEvent(EventType.DESTROY_CITY,
                                    0, 0, 0, 50, 500)); }
                }));
    }

    public void draw() {

        // Update all backend
        for (Button b : buttons) {
            b.update();
        }
        for (Button b : buttons) {
            b.display();
        }
        cityState.update();

        // Start drawing the front end.
        background(255);
        infoDrawer.drawTextBox("Number of Houses: " + cityState.getCityStateParameters().getNumHouses(), 20, height-20, 150);
    }

    public static void main(String... args){
        PApplet.main("CitySimulation");
    }
}
