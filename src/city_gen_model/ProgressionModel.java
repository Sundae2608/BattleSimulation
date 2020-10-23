package city_gen_model;

import city_gen_model.progression.ExponentialFunction;
import city_gen_model.progression.LinearFunction;
import city_gen_model.city_events.MapEvent;
import city_gen_model.progression.Progression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressionModel {

    private Map<CityParamType, List<Progression>> progressionFunctions;
    private Map<MapEvent, List<Progression>> eventProgressionMap;

    public ProgressionModel() {
        progressionFunctions = new HashMap<>();

        for (CityParamType cityParamType : CityParamType.values()) {
            progressionFunctions.put(cityParamType, new ArrayList<>());
        }

        progressionFunctions.get(CityParamType.HOUSE).add(new ExponentialFunction(1.01));
        progressionFunctions.get(CityParamType.PERSON).add(new LinearFunction(1));

        eventProgressionMap = new HashMap<>();
    }

    public void registerEvent(MapEvent mapEvent) {
        List<Progression> progressionList = new ArrayList<>();
        switch (mapEvent.getMapEventType()) {
            case DESTROY_CITY:
                break;
            case FLOOD:
                break;
            case LOWER_TAX:
                break;
            default:
                break;
        }
        eventProgressionMap.put(mapEvent, progressionList);
    }

    public void update(CityStateParameters cityParams) {
        for (MapEvent event : eventProgressionMap.keySet()){
            event.setInterval(event.getInterval()-1);
            if (event.getInterval() == 0) {
                for (CityParamType paramType : progressionFunctions.keySet()) {
                    progressionFunctions.get(paramType).removeAll(eventProgressionMap.get(event));
                }
            }
        }

        eventProgressionMap.entrySet().removeIf(x -> x.getKey().getInterval() == 0);

        for (CityParamType paramType : progressionFunctions.keySet()) {
            for (Progression func : progressionFunctions.get(paramType)) {
                cityParams.setQuantity(paramType, (int) func.getNextValue(cityParams.getQuantity(paramType)));
            }
        }
    }
}
