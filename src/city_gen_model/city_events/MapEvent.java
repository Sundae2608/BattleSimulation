package city_gen_model.city_events;

import city_gen_model.CityObjectType;
import city_gen_model.progression.LogisticFunction;
import model.events.Event;
import model.events.EventType;

import java.util.HashMap;
import java.util.Map;

public abstract class MapEvent {

    int interval; // number of time step that the event takes effect
    double radius;

    public MapEvent(double inputX, double inputY, double inputZ,
                    int interval, double radius) {
        this.interval = interval;
        this.radius = radius;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public double getRadius() {
        return radius;
    }

    public abstract MapEventType getMapEventType();

    public abstract void modifyFunctions(Map<CityObjectType, LogisticFunction> logisticFunctions);
}
