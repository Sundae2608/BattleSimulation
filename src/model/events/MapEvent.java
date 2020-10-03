package model.events;
import model.algorithms.geometry.map_generation.Progression;

public class MapEvent extends Event {

    int interval; // number of time step that the event takes effect
    double radius;

    Progression progressionFunction; // Each event only affects one entity type, which in turn only
                                                // affects one progression function

    public MapEvent(EventType inputType, double inputX, double inputY, double inputZ,
                    int interval, double radius, Progression function) {
        super(inputType, inputX, inputY, inputZ);
        this.interval = interval;
        this.radius = radius;
        this.progressionFunction = function;
    }

    public int getInterval() {
        return interval;
    }

    public double getRadius() {
        return radius;
    }

    public Progression getProgressionFunction() {
        return progressionFunction;
    }
}
