package city_gen_model.city_events;

import city_gen_model.ProgressionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CityEventScheduler {

    private HashMap<Integer, List<CityEvent>>  timeStepEventMap;

    public CityEventScheduler() {
        timeStepEventMap = new HashMap<>();
    }

    /**
     * Register an event with timeStep
     * @param timeStep
     * @param event
     */
    public void registerEvent(int timeStep, CityEvent event) {
        if (!timeStepEventMap.containsKey(timeStep)) {
            timeStepEventMap.put(timeStep, new ArrayList<>());
        }
        timeStepEventMap.get(timeStep).add(event);
    }

    /**
     * Get a list of events happen at a timeStep
     * @param timeStep
     * @return
     */
    public List<CityEvent> getEvents(int timeStep) {
        return timeStepEventMap.get(timeStep);
    }
}
