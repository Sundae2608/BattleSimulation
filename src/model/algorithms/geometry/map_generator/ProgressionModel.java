package model.algorithms.geometry.map_generator;

import model.map_objects.House;

public class ProgressionModel {

    private ProgressionFunction<House> houseProgressionFunction;

    public ProgressionModel() {
        // Empty constructor
    }

    public ProgressionFunction<House> getHouseProgressionFunction() {
        return houseProgressionFunction;
    }

    public void setHouseProgressionFunction(ProgressionFunction<House> houseProgressionFunction) {
        this.houseProgressionFunction = houseProgressionFunction;
    }
}
