package singles;

import constants.SwordmanConstants;
import units.BaseUnit;
import units.PoliticalFaction;

public class SwordmanSingle extends BaseSingle {

    public SwordmanSingle(double xInit, double yInit, PoliticalFaction faction, BaseUnit inputUnit, int index) {

        // Parent constructor
        super();

        // Positional attribues
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;
        singleIndex = index;

        // Set up political faction
        politicalFaction = faction;
        unit = inputUnit;

        // Set default stat up stats
        speedStat = SwordmanConstants.SPEED_STAT;
        deceleratingDistStat = SwordmanConstants.DECELERATING_DIST;
        combatDelayStat = SwordmanConstants.COMBAT_DELAY_STAT;
        combatRangeStat = SwordmanConstants.COMBAT_RANGE;
        healthStat = SwordmanConstants.HEALTH_STAT;
        attackStat = SwordmanConstants.ATTACK_STAT;

        // Default constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        combatDelay = combatDelayStat;
        state = SingleState.IN_POSITION;
        size = SwordmanConstants.SINGLE_SIZE;

        // Preprocessing some internal stats
        preprocessStats();
    }
}
