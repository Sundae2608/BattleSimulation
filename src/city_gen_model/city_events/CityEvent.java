package city_gen_model.city_events;

import city_gen_model.CityObjectType;
import city_gen_model.ProgressionModel;
import city_gen_model.progression.LogisticFunction;
import model.events.Event;
import model.events.EventType;

import java.util.HashMap;
import java.util.Map;

public abstract class CityEvent {

    int interval; // number of time step that the event takes effect
    double radius;
    int timeDelay;

    double positionX;
    double positionY;
    double positionZ;

    /**
     * Constructor
     * City Event that takes effect immediately
     * @param inputX
     * @param inputY
     * @param inputZ
     * @param impactInterval
     * @param radius
     */
    public CityEvent(double inputX, double inputY, double inputZ,
                     int impactInterval, double radius) {
        this.interval = impactInterval;
        this.radius = radius;
        this.timeDelay = 0;

        this.positionX = inputX;
        this.positionY = inputY;
        this.positionZ = inputZ;
    }

    /**
     * Constructor
     * City event that takes effect after some amount of time, indicated by timeDelay value
     * @param inputX
     * @param inputY
     * @param inputZ
     * @param impactInterval
     * @param radius
     * @param timeDelay
     */
    public CityEvent(double inputX, double inputY, double inputZ,
                    int impactInterval, double radius, int timeDelay) {
        this.interval = impactInterval;
        this.radius = radius;
        this.timeDelay = timeDelay;

        this.positionX = inputX;
        this.positionY = inputY;
        this.positionZ = inputZ;
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

    public int getTimeDelay() {
        return timeDelay;
    }

    public abstract CityEventType getCityEventType();

    public abstract void updateModel(ProgressionModel model);
}
