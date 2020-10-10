import city_gen_model.CityState;
import city_gen_model.ProgressionModel;
import model.events.EventBroadcaster;
import model.events.EventType;
import model.events.MapEvent;
import processing.core.PApplet;
import view.components.Button;
import view.components.CustomProcedure;
import view.drawer.InfoDrawer;

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

    public void settings() {
        size(width, height);
    }

    public void setup() {
        infoDrawer = new InfoDrawer(this);
        eventBroadcaster = new EventBroadcaster();
        cityState = new CityState(eventBroadcaster);
        progressionModel = new ProgressionModel();
        buttons = new ArrayList<>();
        buttons.add(new Button("Trigger Decay Event",
                width-180, height-50, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { eventBroadcaster.broadcastEvent(
                            new MapEvent(EventType.DESTROY_CITY,
                                    0, 0, 0, 200, 500)); }
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
        infoDrawer.drawTextBox("City State: " + cityState.getNumHouses(), 20, height-20, 150);
        cityState.update();
    }

    public static void main(String... args){
        PApplet.main("CitySimulation");
    }
}
