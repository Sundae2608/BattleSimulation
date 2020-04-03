package singles;

import constants.CavalryConstants;
import units.BaseUnit;
import units.PoliticalFaction;

public class CavalrySingle extends BaseSingle {

    public CavalrySingle(double xInit, double yInit, PoliticalFaction faction, BaseUnit inputUnit, int index) {

        // Parent constructor
        super();

        // Position attribute
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;
        singleIndex = index;

        // Set up political faction
        politicalFaction = faction;
        unit = inputUnit;

        // Set default stat up stats
        speedStat = CavalryConstants.SPEED_STAT;
        deceleratingDistStat = CavalryConstants.DECELERATING_DIST;
        combatDelayStat = CavalryConstants.COMBAT_DELAY_STAT;
        combatRangeStat = CavalryConstants.COMBAT_RANGE;
        healthStat = CavalryConstants.HEALTH_STAT;
        attackStat = CavalryConstants.ATTACK_STAT;

        // Default constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        combatDelay = combatDelayStat;
        state = SingleState.IN_POSITION;
        size = CavalryConstants.SINGLE_SIZE;

        // Preprocessing some internal stats
        preprocessStats();
    }
}
