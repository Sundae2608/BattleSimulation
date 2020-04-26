package model.settings;

public class GameSettings {

    // Turning on cavalry collision makes much more beautiful cavalry behavior, but it also drains resources.
    private boolean cavalryCollision;

    // Check collision from border inward. This helps reduce the number of objects checking for collision, but also
    private boolean borderInwardCollision;

    // Apply change in speed based on terrain
    private boolean applyTerrainModifier;

    public GameSettings() {}

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
}