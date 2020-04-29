package model.objects;

import model.utils.MathUtils;

public class Arrow extends BaseObject {

    // Arrow speed
    protected double speed;
    protected double damage;

    // Positions
    // Precalculate array positions for all its life type
    protected double[][] pos;
    protected boolean[] impact;
    protected int index;

    /**
     * Initialize instances of arrow with all positions pre-calculated.
     */
    public Arrow(double inputX, double inputY,
                 double goalX, double goalY, double speed,
                 double inputDamage, double angleVariation, double impactLifeTime) {
        damage = inputDamage;
        angle = MathUtils.atan2(goalY - inputY, goalX - inputX);
        index = 0;
        if (angleVariation != 0) {
            angle += MathUtils.randDouble(-angleVariation, angleVariation);
        }
        double dx = MathUtils.quickCos((float) angle) * speed;
        double dy = MathUtils.quickSin((float) angle) * speed;

        // Calculate arrow life
        int lifeTime = Math.min(
                (int) Math.abs(Math.ceil((goalX - inputX) / dx)),
                (int) Math.abs(Math.ceil((goalY - inputY) / dy))) + 1;
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
            return;
        }
        x = pos[index][0];
        y = pos[index][1];
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
}