package singles;

import constants.PhalanxConstants;
import units.BaseUnit;
import units.PoliticalFaction;

public class PhalanxSingle extends BaseSingle {
    public PhalanxSingle(double xInit, double yInit, PoliticalFaction faction, BaseUnit inputUnit, int index) {

        // Parent constructor
        super();

        // Positional attribute
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;

        // Set up political faction
        politicalFaction = faction;
        unit = inputUnit;
        singleIndex = index;

        // Set default stat up stats
        speedStat = PhalanxConstants.SPEED_STAT;
        deceleratingDistStat = PhalanxConstants.DECELERATING_DIST;
        combatDelayStat = PhalanxConstants.COMBAT_DELAY_STAT;
        combatRangeStat = PhalanxConstants.COMBAT_RANGE;
        healthStat = PhalanxConstants.HEALTH_STAT;
        attackStat = PhalanxConstants.ATTACK_STAT;

        // Default constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        combatDelay = combatDelayStat;
        state = SingleState.IN_POSITION;
        size = PhalanxConstants.SINGLE_SIZE;

        // Preprocessing some internal stats
        preprocessStats();
    }
}
