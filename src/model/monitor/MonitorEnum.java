package model.monitor;

public enum MonitorEnum {

    // Collision enums
    COLLISION_TROOPS("Troops collision"),
    COLLISION_TROOP_AND_TERRAIN("Troop-vs-terrain collision"),
    COLLISION_TROOP_AND_CONSTRUCT("Troop-vs-construct collision"),
    COLLISION_TROOP_AND_TREE("Troop-vs-tree collision"),
    COLLISION_OBJECT("Object collision"),

    // Formation changes
    WRONG_FORMATION_CHANGES("Wrong formation changes");

    private String name;

    MonitorEnum(String enumName) {
        name = enumName;
    }

    public String toString() {
        return name;
    }
}
