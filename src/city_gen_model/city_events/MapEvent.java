package city_gen_model.city_events;

import model.events.Event;
import model.events.EventType;

public class MapEvent {

    int interval; // number of time step that the event takes effect
    double radius;
    MapEventType mapEventType;

    public MapEvent(MapEventType inputType, double inputX, double inputY, double inputZ,
                    int interval, double radius) {
        this.interval = interval;
        this.radius = radius;
        this.mapEventType = inputType;
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

    public MapEventType getMapEventType() { return mapEventType; }
}
