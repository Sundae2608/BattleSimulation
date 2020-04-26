package model.settings;

public class GameSettings {

    // Turning on cavalry collision makes much more beautiful cavalry behavior, but it also drains resources.
    private boolean cavalryCollision;

    // Check collision from border inward. This helps reduce the number of objects checking for collision, but also
    private boolean borderInwardCollision;

    // Draw troops in danger with different color
    private boolean drawTroopInDanger;  // Applicable only if borderInwardCollision == true

    // Draw troops in position with different color
    private boolean drawTroopInPosition;

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

    public boolean isDrawTroopInDanger() {
        return drawTroopInDanger;
    }
    public void setDrawTroopInDanger(boolean drawTroopInDanger) {
        this.drawTroopInDanger = drawTroopInDanger;
    }

    public boolean isDrawTroopInPosition() {
        return drawTroopInPosition;
    }
    public void setDrawTroopInPosition(boolean drawTroopInPosition) {
        this.drawTroopInPosition = drawTroopInPosition;
    }
}