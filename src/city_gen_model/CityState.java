package city_gen_model;

import city_gen_model.city_events.MapEvent;
import city_gen_model.city_events.MapEventBroadcaster;
import city_gen_model.city_events.MapEventListener;
import model.events.*;

public class CityState extends MapEventListener {

    CityStateParameters cityStateParameters;
    ProgressionModel model;
    private int currentTimeStep;

    public CityState(MapEventBroadcaster inputBroadcaster, CityStateParameters cityStateParameters) {
        super(inputBroadcaster);

        this.model = new ProgressionModel();
        this.cityStateParameters = cityStateParameters;
        this.currentTimeStep = 0;
    }

    public CityStateParameters getCityStateParameters() {
        return cityStateParameters;
    }

    @Override
    protected void listenEvent(MapEvent event) {
        model.registerEvent(event);
    }

    public void update() {
        currentTimeStep++;
        model.update(cityStateParameters);
    }
}
