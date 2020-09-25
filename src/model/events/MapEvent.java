package model.events;

import model.algorithms.geometry.EntityType;

public class MapEvent extends Event {

    int interval; // number of time step that the event takes effect
    double radius;
    EntityType entityType; // each event only affects one entity type, which in turn only affects one progression function

    public MapEvent(EventType inputType, double inputX, double inputY, double inputZ,
                    int interval) {
        super(inputType, inputX, inputY, inputZ);
        this.interval = interval;
    }

    public int getTimeStep() {
        return interval;
    }
}
