package city_gen_model;

import city_gen_model.house_progression.DecayHouseProgression;
import city_gen_model.house_progression.NominalHouseProgression;
import model.events.MapEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressionModel {

    private List<Progression> houseProgressions;
    private Map<MapEvent, List<Progression>> eventProgressionMap;

    public ProgressionModel() {
        houseProgressions = new ArrayList<>();
        houseProgressions.add(new NominalHouseProgression());

        eventProgressionMap = new HashMap<>();
    }

    public void registerEvent(MapEvent mapEvent) {
        List<Progression> progressionList = new ArrayList<>();
        switch (mapEvent.getEventType()) {
            case DESTROY_CITY:
                progressionList.add(new DecayHouseProgression());
                break;
            default:
                break;
        }
        eventProgressionMap.put(mapEvent, progressionList);
        houseProgressions.addAll(eventProgressionMap.get(mapEvent));
    }

    public void update(CityStateParameters cityParams) {
        for (MapEvent event : eventProgressionMap.keySet()){
            event.setInterval(event.getInterval()-1);
            if (event.getInterval() == 0) {
                houseProgressions.removeAll(eventProgressionMap.get(event));
            }
        }

        eventProgressionMap.entrySet().removeIf(x -> x.getKey().getInterval() == 0);

        for (Progression proc : houseProgressions) {
            cityParams.setNumHouses(proc.progress(cityParams.getNumHouses()));
        }
    }
}
