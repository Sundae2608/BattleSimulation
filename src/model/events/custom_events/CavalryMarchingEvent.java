package model.events.custom_events;

import model.events.Event;
import model.events.EventType;

public class CavalryMarchingEvent extends Event {
    int numSingles;
    public CavalryMarchingEvent(double inputX, double inputY, double inputZ, int inputNumSoldiers) {
        super(EventType.CAVALRY_RUNNING, inputX, inputY, inputZ);
        numSingles = inputNumSoldiers;
    }

    public int getNumSingles() {
        return numSingles;
    }
}
