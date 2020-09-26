package model.singles;

import model.enums.SingleState;
import model.units.BaseUnit;
import model.enums.PoliticalFaction;

public class SwordmanSingle extends BaseSingle {

    public SwordmanSingle(double xInit,
                          double yInit,
                          PoliticalFaction faction,
                          BaseUnit inputUnit,
                          SingleStats inputSingleStats) {

        // Parent constructor
        super(inputSingleStats, inputUnit);

        // Positional attribues
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;

        // Set up political faction
        politicalFaction = faction;

        // Set default stat up stats
        singleStats = inputSingleStats;

        // Default model.constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        state = SingleState.IN_POSITION;
    }
}
