package model.objects;

import model.utils.MathUtils;
import model.utils.PhysicUtils;
import javafx.util.Pair;

public class Ballista extends Projectile {

    // Arrow speed
    double speed;
    double damage;
    double explosionDamage;
    double explosionRange;
    double explosionPush;
    double pushForce;

    // Positions
    // Precalculate array positions for all its life type
    double[][] pos;
    Double[] heightOverTime;
    boolean[] impact;
    boolean touchGround;
    int index;

    /**
     * Initialize instances of arrow with all positions pre-calculated.
     */
    public Ballista(double inputX, double inputY,
                    double goalX, double goalY, double speed,
                    double inputDamage, double inputExplosionDamage, double inputExplosionRange,
                    double inputExplosionPush, double inputPushForce,
                    double angleVariation, double impactLifeTime) {
        damage = inputDamage;
        explosionRange = inputExplosionRange;
        explosionPush = inputExplosionPush;
        explosionDamage = inputExplosionDamage;
        pushForce = inputPushForce;
        angle = MathUtils.atan2(goalY - inputY, goalX - inputX);
        index = 0;
        if (angleVariation != 0) {
            angle += MathUtils.randDouble(-angleVariation, angleVariation);
        }

        // Calculate arrow height and lifetime
        double distance = Math.sqrt((goalX - inputX)*(goalX - inputX) + (goalY - inputY)*(goalY - inputY));
        Pair<Double, Double[]> outputPair = PhysicUtils.calculateProjectileArchGivenSpeedAndDist(speed, distance);
        heightOverTime = outputPair.getValue();

        double dx = MathUtils.quickCos((float) angle) * outputPair.getKey();
        double dy = MathUtils.quickSin((float) angle) * outputPair.getKey();

        int lifeTime = heightOverTime.length;
        int impactTime = (int) Math.max(lifeTime - impactLifeTime, 0);

        // Precalculate all positions
        pos = new double[lifeTime][2];
        impact = new boolean[lifeTime];
        double currX = inputX;
        double currY = inputY;
        for (int i = 0; i < lifeTime; i++) {
            pos[i][0] = currX;
            pos[i][1] = currY;
            currX += dx;
            currY += dy;
        }
        for (int i = impactTime; i < lifeTime; i++) {
            impact[i] = true;
        }

        // Set initial arrow state
        alive = true;
        impactful = impact[index];
        x = pos[0][0];
        y = pos[0][1];
        height = heightOverTime[0];
    }

    /**
     * Update the status of the arrow.
     */
    @Override
    public void update() {
        if (!alive) return;
        index++;
        if (index >= pos.length) {
            alive = false;
            touchGround = false;
            return;
        } else {
            touchGround = true;
        }
        x = pos[index][0];
        y = pos[index][1];
        height = heightOverTime[index];
    }

    /**
     * Indicate whether the arrow is slow enough to deal damange.
     */
    @Override
    public boolean isImpactful() {
        return impact[index];
    }

    public double getSpeed() {
        return speed;
    }

    public double[][] getPos() {
        return pos;
    }

    public boolean[] getImpact() {
        return impact;
    }

    public int getIndex() {
        return index;
    }

    public double getDamage() {
        return damage;
    }

    public double getExplosionDamage() {
        return explosionDamage;
    }

    public double getExplosionRange() {
        return explosionRange;
    }

    public double getExplosionPush() {
        return explosionPush;
    }

    public double getPushForce() {
        return pushForce;
    }

    public boolean isTouchGround() {
        return touchGround;
    }
}
