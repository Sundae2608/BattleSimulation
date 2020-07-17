package model.settings;

public class GameSettings {

    // Turning on cavalry collision makes much more beautiful cavalry behavior, but it also drains resources.
    private boolean cavalryCollision;

    // Check collision from border inward. This helps reduce the number of objects checking for collision, but also
    private boolean borderInwardCollision;

    // Apply change in speed based on terrain
    private boolean applyTerrainModifier;

    // Apply ally collision. This option will makes the game more realistic, but at a pretty big resource cost.
    private boolean allyCollision;

    // Apply collision checking only in combat.
    private boolean collisionCheckingOnlyInCombat;

    // Apply flanking mechanics to first liner who hasn't seen battle yet.
    private boolean enableFlankingMechanics;

    public GameSettings() {}

    public boolean isAllyCollision() {
        return allyCollision;
    }

    public void setAllyCollision(boolean allyCollision) {
        this.allyCollision = allyCollision;
    }

    public boolean isCollisionCheckingOnlyInCombat() {
        return collisionCheckingOnlyInCombat;
    }

    public void setCollisionCheckingOnlyInCombat(boolean collisionCheckingOnlyInCombat) {
        this.collisionCheckingOnlyInCombat = collisionCheckingOnlyInCombat;
    }

    public boolean isCavalryCollision() {
        return cavalryCollision;
    }
    public void setCavalryCollision(boolean cavalryCollision) {
        this.cavalryCollision = cavalryCollision;
    }

    public boolean isBorderInwardCollision() {
        return borderInwardCollision;
    }
    public void setBorderInwardCollision(boolean borderInwardCollision) {
        this.borderInwardCollision = borderInwardCollision;
    }

    public boolean isApplyTerrainModifier() {
        return applyTerrainModifier;
    }
    public void setApplyTerrainModifier(boolean applyTerrainModifier) {
        this.applyTerrainModifier = applyTerrainModifier;
    }

    public boolean isEnableFlankingMechanics() {
        return enableFlankingMechanics;
    }

    public void setEnableFlankingMechanics(boolean enableFlankingMechanics) {
        this.enableFlankingMechanics = enableFlankingMechanics;
    }
}