package model.projectile_objects;

import model.constants.GameplayConstants;

/**
 * A class of objects that represent projectile that flies so fast that it is more effective to just treatment as a line
 * and perform ray-casting collision. Example of these are all kinds of bullets.
 */
public class HitscanObject {

    // Starting position of the hitscan object.
    double startX;
    double startY;
    double startZ;

    // The angle in which the hitscan object aims at
    double theta;
    double phi;

    // The range of the hitscan object. Min range is helpful to:
    // 1 - Avoid hitting allies.
    // 2 - Effectively simulate the fact that different weapons can have a different set of range that is effective.
    double minRange;
    double maxRange;

    // Damage of the hitscan
    double damage;

    // Life time
    int lifetime;
    boolean impactful;

    public HitscanObject(
            double x, double y, double z, double theta, double phi, double minRange, double maxRange, double damage) {
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.theta = theta;
        this.phi = phi;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.damage = damage;
        this.lifetime = GameplayConstants.BULLET_LIFETIME;
        this.impactful = true;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getStartZ() {
        return startZ;
    }

    public double getTheta() {
        return theta;
    }

    public double getPhi() {
        return phi;
    }

    public double getMinRange() {
        return minRange;
    }

    public double getMaxRange() {
        return maxRange;
    }

    public double getDamage() {
        return damage;
    }

    public void decrementLifetime() {
        if (lifetime > 0) {
            lifetime--;
        }
    }

    public int getLifetime() {
        return lifetime;
    }

    public boolean isImpactful() {
        return impactful;
    }

    public void setImpactful(boolean impactful) {
        this.impactful = impactful;
    }
}
