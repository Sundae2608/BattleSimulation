package model.events;

public class Event {
    EventType eventType;

    // Position where the event happens
    double x;
    double y;
    double z;

    public Event(EventType inputType, double inputX, double inputY, double inputZ) {
        eventType = inputType;
        x = inputX;
        y = inputY;
        z = inputZ;
    }

    public EventType getEventType() {
        return eventType;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
