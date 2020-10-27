package city_gen_model;

import city_gen_model.progression.ExponentialFunction;
import city_gen_model.progression.LinearFunction;
import city_gen_model.city_events.MapEvent;
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

        for (CityParamType cityParamType : CityParamType.values()) {
            eventProgressionFunctions.put(cityParamType, new ArrayList<>());

            switch (cityParamType) {

                case PERSON:
                    defaultProgressionFunctions.put(cityParamType, new ExponentialFunction(1.02));
                    break;
                case HOUSE:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(1));
                    break;
                case MARKET:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(2));
                    break;
                case FARM:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(3));
                    break;
                case SCHOOL:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(4));
                    break;
                case RELIGIOUS_BUILDING:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(5));
                    break;
                case GOVERNMENT_BUILDING:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(6));
                    break;
                case FACTORY:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(7));
                    break;
                case COST_OF_LIVING:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(8));
                    break;
                default:
                    defaultProgressionFunctions.put(cityParamType, new LinearFunction(20));
            }
        }

        eventProgressionMap = new HashMap<>();

        cityStateParameters = cityParams;
    }

    public void registerEvent(MapEvent mapEvent) {
        List<Progression> progressionList = new ArrayList<>();
        switch (mapEvent.getMapEventType()) {
            case DESTROY_CITY:
                addFunction(mapEvent, CityParamType.HOUSE, new LinearFunction(-4));
                break;
            case FLOOD:
                cityStateParameters.setQuantity(CityParamType.HOUSE,
                        cityStateParameters.getQuantity(CityParamType.HOUSE)/2);
                addFunction(mapEvent, CityParamType.HOUSE, new LinearFunction(-1));
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

    public void update() {
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
                cityStateParameters.setQuantity(paramType, (int) defaultProgressionFunctions.get(paramType)
                        .getNextValue(cityStateParameters.getQuantity(paramType)));
            } else {
                for (Progression func : eventProgressionFunctions.get(paramType)) {
                    cityStateParameters.setQuantity(paramType, (int) func.getNextValue(cityStateParameters
                            .getQuantity(paramType)));
                }
            }

        }
    }
}
