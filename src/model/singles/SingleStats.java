package model.singles;

import model.enums.PoliticalFaction;
import model.enums.UnitType;

public class SingleStats {
    public PoliticalFaction faction;
    public UnitType unitType;

    // Physical attribute
    public double radius;
    public double collisionRadius;
    public double speed;
    public double hp;
    public double deceleration;
    public double acceleration;
    public double rotationSpeed;

    // Distance stats
    // Variation distance attributes, which are used to calculate change in behaviors
    // and goals that are distance-based
    public double outOfReachDist;
    public double outOfReachSpeed;
    public double standingDist;
    public double nonRotationDist;

    // Battle attributes
    public double attack;
    public double combatRange;
    public int combatDelay;
    public double sustainRecovery;

    // Archer stats
    // TODO: All archer-specific variables should have an archer prefix.
    public int reloadDelay;
    public int boredDelay;
    public double arrowSpeed;
    public double angleVariation;
    public double arrowDamage;
    public int impactLifetime;
    public double firingRange;
    public double squaredFiringRange;
    public double arrowSize;

    // Balista stats
    public double balistaDamage;
    public double balistaSpeed;
    public double explosionDamage;
    public double explosionRange;
    public double explosionPush;
}
