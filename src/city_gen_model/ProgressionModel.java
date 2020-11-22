package city_gen_model;

import city_gen_model.progression.ExponentialFunction;
import city_gen_model.progression.LinearFunction;
import city_gen_model.city_events.MapEvent;
import city_gen_model.progression.LogisticFunction;
import city_gen_model.progression.Progression;

import java.util.*;

public class ProgressionModel {

    private CityStateParameters cityStateParameters;

    private Map<CityParamType, Progression> defaultProgressionFunctions;
    private Map<CityParamType, List<Progression>> eventProgressionFunctions;
    private Map<MapEvent, List<Progression>> eventProgressionMap;

    public ProgressionModel(CityStateParameters cityParams) {
        defaultProgressionFunctions = new HashMap<>();
        eventProgressionFunctions = new HashMap<>();
        eventProgressionMap = new HashMap<>();
        cityStateParameters = cityParams;

        for (CityParamType cityParamType : CityParamType.values()) {
            eventProgressionFunctions.put(cityParamType, new ArrayList<>());

            double relativeGrowthCoefficient = cityStateParameters.getRelativeGrowthCoefficient(cityParamType);
            double capacity = cityStateParameters.getCapacity(cityParamType);

            defaultProgressionFunctions.put(cityParamType, new LogisticFunction(relativeGrowthCoefficient, capacity));
        }
    }

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
        for (MapEvent event : eventProgressionMap.keySet()){
            event.setInterval(event.getInterval()-1);
            if (event.getInterval() == 0) {
                for (CityParamType paramType : eventProgressionFunctions.keySet()) {
                    eventProgressionFunctions.get(paramType).removeAll(eventProgressionMap.get(event));
                }
            }
        }

        eventProgressionMap.entrySet().removeIf(x -> x.getKey().getInterval() == 0);

        for (CityParamType paramType : eventProgressionFunctions.keySet()) {
            if (eventProgressionFunctions.get(paramType).size() == 0) {
                cityStateParameters.setQuantity(paramType, defaultProgressionFunctions.get(paramType)
                        .getNextValue(cityStateParameters.getQuantity(paramType), numMonths));
            } else {
                for (Progression func : eventProgressionFunctions.get(paramType)) {
                    cityStateParameters.setQuantity(paramType, func.getNextValue(cityStateParameters
                            .getQuantity(paramType), numMonths));
                }
            }
        }

        if (!cityStateParameters.valid()) {
            throw new Exception("City State is not valid.");
        }
    }
}
