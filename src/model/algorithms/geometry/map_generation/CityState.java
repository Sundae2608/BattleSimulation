package model.algorithms.geometry.map_generation;

import model.algorithms.geometry.map_generation.house_progression.DecayHouseProgression;
import model.algorithms.geometry.map_generation.house_progression.NominalHouseProgression;
import model.events.*;

public class CityState extends EventListener {

    int numHouses;

    ProgressionModel initialModel;
    ProgressionModel currentModel;

    public CityState(EventBroadcaster inputBroadcaster) {
        super(inputBroadcaster);

        initialModel = new ProgressionModel();
        initialModel.setHouseProgressionFunction(new NominalHouseProgression());

        currentModel = new ProgressionModel();
        currentModel.copy(initialModel);
        numHouses = 1000;
    }

    @Override
    protected void listenEvent(Event e) {
        if (!e.getClass().equals(MapEvent.class)) {
            return;
        }

        //TODO: count down time steps that this event takes effect
        MapEvent mapEvent = (MapEvent) e;

        if (e.getEventType() == EventType.DESTROY_CITY) {
            currentModel.setHouseProgressionFunction(new DecayHouseProgression());
        }
        if (e.getEventType() == EventType.RESET_HOUSE_PROGRESSION) {
            currentModel.setHouseProgressionFunction(initialModel.getHouseProgression());
        }
    }

    public int getNumHouses() {
        return numHouses;
    }

    public void update() {
        numHouses = currentModel.getHouseProgression().progress(numHouses);
    }
}
