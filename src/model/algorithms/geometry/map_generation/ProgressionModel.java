package model.algorithms.geometry.map_generation;

import model.events.EventBroadcaster;

public class ProgressionModel {

    private Progression houseProgression;

    public Progression getHouseProgression() {
        return houseProgression;
    }

    public void setHouseProgressionFunction(Progression houseProgression) {
        this.houseProgression = houseProgression;
    }

    public void copy(ProgressionModel other) {
        if (other.getHouseProgression() != null) {
            setHouseProgressionFunction(other.houseProgression);
        }
    }
}
