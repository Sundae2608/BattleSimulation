package city_gen_model;

import city_gen_model.house_progression.DecayHouseProgression;
import city_gen_model.house_progression.NominalHouseProgression;
import city_gen_model.city_events.MapEvent;
import city_gen_model.house_progression.NominalPopulationProgression;
import city_gen_model.house_progression.PopulationDecrease;
import model.map_objects.House;

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

        progressionFunctions.get(CityParamType.HOUSE).add(new NominalHouseProgression());
        progressionFunctions.get(CityParamType.PERSON).add(new NominalPopulationProgression());

        eventProgressionMap = new HashMap<>();
    }

    public void registerEvent(MapEvent mapEvent) {
        List<Progression> progressionList = new ArrayList<>();
        switch (mapEvent.getMapEventType()) {
            case DESTROY_CITY:
                Progression houseDecrease = new DecayHouseProgression();
                Progression populationDecrease = new PopulationDecrease();
                progressionList.add(houseDecrease);
                progressionList.add(populationDecrease);
                progressionFunctions.get(CityParamType.HOUSE).add(houseDecrease);
                progressionFunctions.get(CityParamType.PERSON).add(populationDecrease);
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
                progressionFunctions.get(CityParamType.HOUSE).removeAll(eventProgressionMap.get(event));
                progressionFunctions.get(CityParamType.PERSON).removeAll(eventProgressionMap.get(event));
            }
        }

        eventProgressionMap.entrySet().removeIf(x -> x.getKey().getInterval() == 0);

        for (List<Progression> progressionList : progressionFunctions.values()) {
            for (Progression func : progressionList) {
                cityParams.setQuantity(CityParamType.HOUSE, func.progress(cityParams.getQuantity(CityParamType.HOUSE)));
                cityParams.setQuantity(CityParamType.PERSON, func.progress(cityParams.getQuantity(CityParamType.PERSON)));
            }
        }
    }
}
