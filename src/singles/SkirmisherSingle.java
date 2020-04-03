package singles;

import constants.SkirmisherConstants;
import units.BaseUnit;
import units.PoliticalFaction;

public class SkirmisherSingle extends BaseSingle {
    public SkirmisherSingle(double xInit, double yInit, PoliticalFaction faction, BaseUnit inputUnit, int index) {

        // Parent constructor
        super();

        // Positional attribute
        x = xInit;
        y = yInit;
        xGoal = xInit;
        yGoal = yInit;
        singleIndex = index;

        // Set up political faction
        politicalFaction = faction;
        unit = inputUnit;
        singleIndex = index;

        // Set default stat up stats
        speedStat = SkirmisherConstants.SPEED_STAT;
        deceleratingDistStat = SkirmisherConstants.DECELERATING_DIST;
        combatDelayStat = SkirmisherConstants.COMBAT_DELAY_STAT;
        combatRangeStat = SkirmisherConstants.COMBAT_RANGE;
        healthStat = SkirmisherConstants.HEALTH_STAT;
        attackStat = SkirmisherConstants.ATTACK_STAT;

        // Default constants;
        speedGoal = 0;
        speed = 0;
        height = 0;
        combatDelay = combatDelayStat;
        state = SingleState.IN_POSITION;
        size = SkirmisherConstants.SINGLE_SIZE;

        // Preprocessing some internal stats
        preprocessStats();
    }
}
