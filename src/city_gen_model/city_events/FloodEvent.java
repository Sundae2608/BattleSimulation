package city_gen_model.city_events;

import city_gen_model.CityObjectType;
import city_gen_model.ProgressionModel;
import city_gen_model.progression.LogisticFunction;

import java.util.Map;

public class FloodEvent extends CityEvent {

    // Impact of the flood event.
    // The rate of change is reduced based on the value of the impact parameter. If the impact is 2, the rate of change
    // will be reduced by a factor of 2. If the impact is 3, the rate of change will be reduced by a factor of 3, etc.
    double impact;

    public FloodEvent(double inputX, double inputY, double inputZ, int interval, double radius, double impact) {
        super(inputX, inputY, inputZ, interval, radius);
        this.impact = impact;
    }

    public FloodEvent(double inputX, double inputY, double inputZ, int interval, double radius, int timeDelay, double impact) {
        super(inputX, inputY, inputZ, interval, radius, timeDelay);
        this.impact = impact;
    }

    @Override
    public CityEventType getCityEventType() {
        return CityEventType.FLOOD;
    }

    @Override
    public void updateModel(ProgressionModel model) {
        model.setRelativeGrowthCoefficient(CityObjectType.HOUSE,
                model.getRelativeGrowthCoefficient(CityObjectType.HOUSE)/impact);
    }
}
