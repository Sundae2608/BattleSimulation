package city_gen_model;

import city_gen_model.progression.ExponentialFunction;
import city_gen_model.progression.LinearFunction;
import city_gen_model.city_events.MapEvent;
import city_gen_model.progression.LogisticFunction;
import city_gen_model.progression.Progression;

import java.util.*;

public class ProgressionModel {

    private CityObjects cityObjects;
    private Map<CityParamType, Progression> defaultProgressionFunctions;
    private Map<CityParamType, List<Progression>> eventProgressionFunctions;
    private Map<MapEvent, List<Progression>> eventProgressionMap;

    public ProgressionModel(CityObjects cityParams) {
        defaultProgressionFunctions = new HashMap<>();
        eventProgressionFunctions = new HashMap<>();
        eventProgressionMap = new HashMap<>();
        cityObjects = cityParams;

        for (CityParamType cityParamType : CityParamType.values()) {
            eventProgressionFunctions.put(cityParamType, new ArrayList<>());

            double relativeGrowthCoefficient = cityObjects.getRelativeGrowthCoefficient(cityParamType);
            double capacity = cityObjects.getCapacity(cityParamType);

            defaultProgressionFunctions.put(cityParamType, new LogisticFunction(relativeGrowthCoefficient, capacity));
        }
    }

    /**
     * Add new progression functions to model
     * In the next t months (determined by the mapEvent parameter), city state will progress according to the newly
     * added functions, instead of the default ones. After t steps, the default progression function will be used.
     * @param mapEvent
     */
    public void registerEvent(MapEvent mapEvent) {
        switch (mapEvent.getMapEventType()) {
            case DESTROY_CITY:
                addFunction(mapEvent, CityParamType.HOUSE, new LinearFunction(-4));
                break;
            case FLOOD:
                addFunction(mapEvent, CityParamType.HOUSE, new LinearFunction(-20));
                break;
            case LOWER_TAX:
                addFunction(mapEvent, CityParamType.PERSON, new ExponentialFunction(1.08));
                addFunction(mapEvent, CityParamType.MARKET, new LinearFunction(20));
                break;
            case AGRICULTURE_CULTIVATION:
                addFunction(mapEvent, CityParamType.FARM, new LinearFunction(20));
                break;
            case PUBLIC_WELFARE:
                addFunction(mapEvent, CityParamType.COST_OF_LIVING, new LinearFunction(-10));
                addFunction(mapEvent, CityParamType.SCHOOL, new LinearFunction(20));
                break;
            case FREE_EXCHANGE_OF_IDEAS:
                addFunction(mapEvent, CityParamType.FACTORY, new LinearFunction(20));
                break;
            case ANTI_HERESY:
                addFunction(mapEvent, CityParamType.RELIGIOUS_BUILDING, new LinearFunction(-20));
                break;
            default:
                break;
        }
    }

    private void addFunction(MapEvent mapEvent, CityParamType cityParamType, Progression function) {
        eventProgressionFunctions.get(cityParamType).add(function);
        if (!eventProgressionMap.containsKey(mapEvent)) {
            eventProgressionMap.put(mapEvent, new ArrayList<>());
        }
        eventProgressionMap.get(mapEvent).add(function);
    }

    public void update(int numMonths) throws Exception {
        // Decrease the time interval in each map event, and remove the map event if its time interval reaches 0
        for (MapEvent event : eventProgressionMap.keySet()){
            event.setInterval(event.getInterval()-1);
            if (event.getInterval() == 0) {
                for (CityParamType paramType : eventProgressionFunctions.keySet()) {
                    eventProgressionFunctions.get(paramType).removeAll(eventProgressionMap.get(event));
                }
            }
        }
        eventProgressionMap.entrySet().removeIf(x -> x.getKey().getInterval() == 0);

        // For each of the city parameter type, use the default functions if there is no active event. Otherwise,
        // use the functions in eventProgressionFunctions
        for (CityParamType paramType : eventProgressionFunctions.keySet()) {
            if (eventProgressionFunctions.get(paramType).size() == 0) {
                cityObjects.setQuantity(paramType, defaultProgressionFunctions.get(paramType)
                        .getNextValue(cityObjects.getQuantity(paramType), numMonths));
            } else {
                for (Progression func : eventProgressionFunctions.get(paramType)) {
                    cityObjects.setQuantity(paramType, func.getNextValue(cityObjects
                            .getQuantity(paramType), numMonths));
                }
            }
        }
    }
}
