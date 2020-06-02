package model.enums;

public enum UnitState {
    MOVING("Moving"),
    STANDING("Standing"),
    DECELERATING("Decelerating"),
    FIGHTING("Fighting"),
    ROUTING("Routing");

    // String representation of the state
    String string;

    UnitState(String inputString) {
        string = inputString;
    }

    @Override
    public String toString() {
        return string;
    }
}
