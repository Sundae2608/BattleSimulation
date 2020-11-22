package city_gen_model;

import city_gen_model.city_events.MapEvent;
import city_gen_model.city_events.MapEventBroadcaster;
import city_gen_model.city_events.MapEventListener;
import model.events.*;

public class CityState extends MapEventListener {

    CityStateParameters cityStateParameters;
    ProgressionModel model;

    public CityState(MapEventBroadcaster inputBroadcaster, CityStateParameters cityStateParameters) {
        super(inputBroadcaster);

        this.model = new ProgressionModel(cityStateParameters);
        this.cityStateParameters = cityStateParameters;
    }

    public CityStateParameters getCityStateParameters() {
        return cityStateParameters;
    }

    @Override
    protected void listenEvent(MapEvent event) {
        model.registerEvent(event);
    }

    public void update(int timeSteps) throws Exception {
        model.update(timeSteps);
    }
}
