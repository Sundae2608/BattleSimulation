package model.map_objects;

import model.construct.Construct;
import model.construct.ConstructType;

public class House extends Construct {
    public House(double[][] points) {
        super(ConstructType.HOUSE, points);
    }
}
