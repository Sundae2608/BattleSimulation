package model.events;

public class MapEvent extends Event {

    int interval; // number of time step that the event takes effect
    double radius;

    public MapEvent(EventType inputType, double inputX, double inputY, double inputZ,
                    int interval, double radius) {
        super(inputType, inputX, inputY, inputZ);
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
}
