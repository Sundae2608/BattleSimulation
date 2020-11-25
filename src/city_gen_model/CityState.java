package city_gen_model;

import city_gen_model.city_events.MapEvent;
import city_gen_model.city_events.MapEventBroadcaster;
import city_gen_model.city_events.MapEventListener;

public class CityState extends MapEventListener {

    CityObjects cityObjects;
    ProgressionModel model;

    public CityState(MapEventBroadcaster inputBroadcaster, CityObjects cityObjects) {
        super(inputBroadcaster);

        this.model = new ProgressionModel(cityObjects);
        this.cityObjects = cityObjects;
    }

    public CityObjects getCityStateParameters() {
        return cityObjects;
    }

    @Override
    protected void listenEvent(MapEvent event) {
        model.registerEvent(event);
    }

    public void update(int timeSteps) throws Exception {
        model.update(timeSteps);
    }
}
