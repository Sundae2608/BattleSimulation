package singles;

import constants.SlingerConstants;
import units.BaseUnit;
import units.PoliticalFaction;

public class SlingerSingle extends BaseSingle {
    public SlingerSingle(double xInit, double yInit, PoliticalFaction faction, BaseUnit inputUnit, int index) {

        // Parent constructor
        super();

        // Positional index
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;
        singleIndex = index;

        // Set up political faction
        politicalFaction = faction;
        unit = inputUnit;

        // Set default stat up stats
        speedStat = SlingerConstants.SPEED_STAT;
        deceleratingDistStat = SlingerConstants.DECELERATING_DIST;
        combatDelayStat = SlingerConstants.COMBAT_DELAY_STAT;
        combatRangeStat = SlingerConstants.COMBAT_RANGE;
        healthStat = SlingerConstants.HEALTH_STAT;
        attackStat = SlingerConstants.ATTACK_STAT;

        // Default constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        combatDelay = combatDelayStat;
        state = SingleState.IN_POSITION;
        size = SlingerConstants.SINGLE_SIZE;

        // Preprocessing some internal stats
        preprocessStats();
    }
}
