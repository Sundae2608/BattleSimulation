package city_gen_model;

import city_gen_model.city_events.CityEvent;
import city_gen_model.progression.LogisticFunction;

import java.util.*;

public class ProgressionModel {

    private CityObjects cityObjects;
    private Map<CityObjectType, LogisticFunction> logisticFunctions;
    private List<CityEvent> cityEvents;

    public ProgressionModel(CityObjects cityObjects) {
        logisticFunctions = new HashMap<>();
        cityEvents = new ArrayList<>();
        this.cityObjects = cityObjects;

        for (CityObjectType cityObjectType : CityObjectType.values()) {
            double relativeGrowthCoefficient = this.cityObjects.getRelativeGrowthCoefficient(cityObjectType);
            double capacity = this.cityObjects.getCapacity(cityObjectType);

            logisticFunctions.put(cityObjectType, new LogisticFunction(relativeGrowthCoefficient, capacity));
        }
    }

    /**
     * Add new progression functions to model.
     * @param mapEvent
     */
    public void registerEvent(CityEvent mapEvent) {
        cityEvents.add(mapEvent);
    }

    public void update(int numMonths) {
        // Remaining city events modify current logistic functions
        for (CityEvent cityEvent : cityEvents) {
            cityEvent.modifyFunctions(logisticFunctions);
        }

        // Decrease the time interval in each map event, and remove the map event if its time interval reaches 0
        for (CityEvent event : cityEvents) {
            event.setInterval(event.getInterval() - 1);
        }
        cityEvents.removeIf(x -> x.getInterval() == 0);

        // For each of the city parameter type, use the default functions if there is no active event. Otherwise,
        // use the functions in eventProgressionFunctions
        for (CityObjectType cityObjectType : CityObjectType.values()) {
            cityObjects.setQuantity(cityObjectType, logisticFunctions.get(cityObjectType).getNextValue(cityObjects
                    .getQuantity(cityObjectType), numMonths));
        }
    }
}
