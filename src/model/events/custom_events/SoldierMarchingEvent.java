package model.events.custom_events;

import model.events.Event;
import model.events.EventType;

public class SoldierMarchingEvent extends Event {
    int numSingles;
    public SoldierMarchingEvent(double inputX, double inputY, double inputZ, int inputNumSoldiers) {
        super(EventType.SOLDIER_MARCHING, inputX, inputY, inputZ);
        numSingles = inputNumSoldiers;
    }

    public int getNumSingles() {
        return numSingles;
    }
}
