package model.singles;

import model.enums.SingleState;
import model.units.BaseUnit;
import model.enums.PoliticalFaction;

public class SlingerSingle extends BaseSingle {
    public SlingerSingle(double xInit,
                         double yInit,
                         PoliticalFaction faction,
                         BaseUnit inputUnit,
                         SingleStats inputSingleStats,
                         int index) {

        // Parent constructor
        super(inputSingleStats);

        // Positional index
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;

        // Set up political faction
        politicalFaction = faction;
        unit = inputUnit;

        // Set default stat up stats
        singleStats = inputSingleStats;

        // Default model.constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        state = SingleState.IN_POSITION;
    }
}
