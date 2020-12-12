package city_gen_model;

import city_gen_model.city_events.CityEvent;
import city_gen_model.city_events.CityEventBroadcaster;
import city_gen_model.city_events.CityEventListener;

public class CityState extends CityEventListener {

    CityObjects cityObjects;
    ProgressionModel model;

    public CityState(CityEventBroadcaster inputBroadcaster, CityObjects cityObjects) {
        super(inputBroadcaster);

        this.model = new ProgressionModel(cityObjects);
        this.cityObjects = cityObjects;
    }

    public CityObjects getCityStateParameters() {
        return cityObjects;
    }

    @Override
    protected void listenEvent(CityEvent event) {
        model.registerEvent(event);
    }

    public void update(int timeSteps) throws Exception {
        model.update(timeSteps);
    }
}
