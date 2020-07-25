package model.singles;

import model.enums.PoliticalFaction;
import model.enums.UnitType;

public class SingleStats {
    public PoliticalFaction faction;
    public UnitType unitType;

    // Physical attribute
    public double mass;
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
    public double defense;
    public double combatRange;
    public int combatDelay;
    public double sustainRecovery;

    // Archer stats
    // TODO: All archer-specific variables should have an archer prefix.
    public int reloadDelay;
    public int boredDelay;
    public double angleVariation;
    public int impactLifetime;
    public double firingRange;
    public double squaredFiringRange;
    public double arrowSize;
    public double arrowDamage;
    public double arrowSpeed;
    public double arrowPushDist;

    // Balista stats
    public double ballistaDamage;
    public double ballistaSpeed;
    public double ballistaPushForce;
    public double ballistaExplosionDamage;
    public double ballistaExplosionRange;
    public double ballistaExplosionPush;

    // Catapult stats
    public double catapultDamage;
    public double catapultSpeed;
    public double catapultPushForce;
    public double catapultExplosionDamage;
    public double catapultExplosionRange;
    public double catapultExplosionPush;
}
