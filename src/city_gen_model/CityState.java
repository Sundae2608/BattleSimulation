package city_gen_model;

import city_gen_model.house_progression.DecayHouseProgression;
import model.events.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityState extends EventListener {

    CityStateParameters cityStateParameters;
    ProgressionModel model;

    public CityState(EventBroadcaster inputBroadcaster, CityStateParameters cityStateParameters) {
        super(inputBroadcaster);

        this.model = new ProgressionModel();
        this.cityStateParameters = cityStateParameters;
    }

    public CityStateParameters getCityStateParameters() {
        return cityStateParameters;
    }

    @Override
    protected void listenEvent(Event e) {
        if (!e.getClass().equals(MapEvent.class)) {
            return;
        }
        model.registerEvent((MapEvent) e);
    }


    public void update() {
        model.update(cityStateParameters);
    }
}
