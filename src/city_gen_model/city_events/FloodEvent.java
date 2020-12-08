package city_gen_model.city_events;

import city_gen_model.CityObjectType;
import city_gen_model.progression.LogisticFunction;

import java.util.Map;

public class FloodEvent extends MapEvent {

    // Impact of the flood event.
    // The rate of change is reduced based on the value of the impact parameter. If the impact is 2, the rate of change
    // will be reduced by a factor of 2. If the impact is 3, the rate of change will be reduced by a factor of 3, etc.
    double impact;

    public FloodEvent(double inputX, double inputY, double inputZ, int interval, double radius, double impact) {
        super(inputX, inputY, inputZ, interval, radius);
        this.impact = impact;
    }

    @Override
    public MapEventType getMapEventType() {
        return MapEventType.FLOOD;
    }

    @Override
    public void modifyFunctions(Map<CityObjectType, LogisticFunction> logisticFunctions) {
        logisticFunctions.get(CityObjectType.HOUSE).
                setRelativeGrowthCoefficient(logisticFunctions.get(CityObjectType.HOUSE)
                        .getRelativeGrowthCoefficient()/impact);
    }
}
