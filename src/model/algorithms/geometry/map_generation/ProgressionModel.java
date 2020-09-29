package model.algorithms.geometry.map_generation;

public class ProgressionModel {

    private Progression houseProgression;

    public ProgressionModel() {
        // Empty constructor
    }

    public Progression getHouseProgression() {
        return houseProgression;
    }

    public void setHouseProgressionFunction(Progression houseProgression) {
        this.houseProgression = houseProgression;
    }
}
