package city_gen_model;

import city_gen_model.house_progression.DecayHouseProgression;
import city_gen_model.house_progression.NominalHouseProgression;
import model.events.*;


import java.util.ArrayList;
import java.util.List;

public class CityState extends EventListener {

    int numHouses;

    ProgressionModel initialModel;
    ProgressionModel currentModel;

    List<MapEvent> activeEvents;

    public CityState(EventBroadcaster inputBroadcaster) {
        super(inputBroadcaster);

        initialModel = new ProgressionModel();
        initialModel.setHouseProgressionFunction(new NominalHouseProgression());

        currentModel = new ProgressionModel();
        currentModel.copy(initialModel);

        numHouses = 1000;

        activeEvents = new ArrayList<>();
    }

    @Override
    protected void listenEvent(Event e) {
        if (!e.getClass().equals(MapEvent.class)) {
            return;
        }

        MapEvent mapEvent = (MapEvent) e;
        activeEvents.add(mapEvent);
    }

    public int getNumHouses() {
        return numHouses;
    }

    public void update() {

        // Update model
        for (MapEvent event : activeEvents){
            event.setInterval(event.getInterval()-1);

            if (event.getEventType() == EventType.DESTROY_CITY) {
                if (event.getInterval() == 0) {
                    currentModel.setHouseProgressionFunction(initialModel.getHouseProgression());
                }
                else {
                    currentModel.setHouseProgressionFunction(new DecayHouseProgression());
                }
            }
        }

        activeEvents.removeIf(x -> x.getInterval() == 0);
        numHouses = currentModel.getHouseProgression().progress(numHouses);
    }
}
