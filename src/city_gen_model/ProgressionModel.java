package city_gen_model;

import city_gen_model.city_events.MapEvent;
import city_gen_model.progression.LogisticFunction;

import java.util.*;

public class ProgressionModel {

    private CityObjects cityObjects;
    private Map<CityObjectType, Double> defaultRelativeGrowth;
    private Map<CityObjectType, Double> defaultCarryingCapacity;
    private List<MapEvent> mapEvents;

    public ProgressionModel(CityObjects cityObjects) {
        defaultRelativeGrowth = new HashMap<>();
        defaultCarryingCapacity = new HashMap<>();
        mapEvents = new ArrayList<>();
        this.cityObjects = cityObjects;

        for (CityObjectType cityObjectType : CityObjectType.values()) {
            double relativeGrowthCoefficient = this.cityObjects.getRelativeGrowthCoefficient(cityObjectType);
            double capacity = this.cityObjects.getCapacity(cityObjectType);

            defaultRelativeGrowth.put(cityObjectType, relativeGrowthCoefficient);
            defaultCarryingCapacity.put(cityObjectType, capacity);
        }
    }

    /**
     * Add new progression functions to model
     * In the next t months (determined by the mapEvent parameter), city state will progress according to the newly
     * added functions, instead of the default ones. After t steps, the default progression function will be used.
     * @param mapEvent
     */
    public void registerEvent(MapEvent mapEvent) {
        mapEvents.add(mapEvent);
    }

    public void update(int numMonths) {
        // Decrease the time interval in each map event, and remove the map event if its time interval reaches 0
        for (MapEvent event : mapEvents) {
            event.setInterval(event.getInterval() - 1);

        }

        mapEvents.removeIf(mapEvent -> mapEvent.getInterval() == 0);

        // Apply map event parameter
        Map<CityObjectType, LogisticFunction> logisticFunctionMap = new HashMap<>();
        for (CityObjectType cityObjectType : CityObjectType.values()) {
            logisticFunctionMap.put(cityObjectType, new LogisticFunction(defaultRelativeGrowth.get(cityObjectType),
                    defaultCarryingCapacity.get(cityObjectType)));
        }

        for (MapEvent mapEvent : mapEvents) {
            switch (mapEvent.getMapEventType()) {
                case DESTROY_CITY:
                    break;
                case FLOOD:
                    logisticFunctionMap.get(CityObjectType.HOUSE).setRelativeGrowthCoefficient(
                            logisticFunctionMap.get(CityObjectType.HOUSE).getRelativeGrowthCoefficient() / 2);
                    break;
                case LOWER_TAX:
                    break;
                case AGRICULTURE_CULTIVATION:
                    break;
                case PUBLIC_WELFARE:
                    break;
                case FREE_EXCHANGE_OF_IDEAS:
                    break;
                case ANTI_HERESY:
                    break;
            }
        }

        // For each of the city parameter type, use the default functions if there is no active event. Otherwise,
        // use the functions in eventProgressionFunctions
        for (CityObjectType cityObjectType : CityObjectType.values()) {
            cityObjects.setQuantity(cityObjectType, logisticFunctionMap.get(cityObjectType).getNextValue(cityObjects
                    .getQuantity(cityObjectType), numMonths));

        }

    }
}
