package model.events;

public class Event {
    EventType eventType;

    // Position where the event happens
    double x;
    double y;
    double z;
    double angle;

    public Event(EventType inputType, double inputX, double inputY, double inputZ) {
        eventType = inputType;
        x = inputX;
        y = inputY;
        z = inputZ;
    }

    public Event(EventType inputType, double inputX, double inputY, double inputZ, double inputAngle) {
        eventType = inputType;
        x = inputX;
        y = inputY;
        z = inputZ;
        angle = inputAngle;
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

    public double getAngle() {
        return angle;
    }
}
