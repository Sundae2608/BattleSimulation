package singles;

import constants.HorseArcherConstants;
import units.BaseUnit;
import units.PoliticalFaction;

public class HorseArcherSingle extends BaseSingle {

    public HorseArcherSingle(double xInit, double yInit, PoliticalFaction faction, BaseUnit inputUnit, int index) {

        // Parent constructor
        super();

        // Positional attributes
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;

        // Set up political faction
        politicalFaction = faction;
        unit = inputUnit;
        singleIndex = index;

        // Set default stat up stats
        speedStat = HorseArcherConstants.SPEED_STAT;
        deceleratingDistStat = HorseArcherConstants.DECELERATING_DIST;
        combatDelayStat = HorseArcherConstants.COMBAT_DELAY_STAT;
        combatRangeStat = HorseArcherConstants.COMBAT_RANGE;
        healthStat = HorseArcherConstants.HEALTH_STAT;
        attackStat = HorseArcherConstants.ATTACK_STAT;

        // Default constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        combatDelay = combatDelayStat;
        state = SingleState.IN_POSITION;
        size = HorseArcherConstants.SINGLE_SIZE;

        // Preprocessing some internal stats
        preprocessStats();
    }
}
