package model.projectile_objects;

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

    public HitscanObject(double x, double y, double z, double theta, double phi, double minRange, double maxRange) {
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.theta = theta;
        this.phi = phi;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }
}
