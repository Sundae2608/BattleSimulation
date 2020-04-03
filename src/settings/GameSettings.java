package settings;

import constants.DrawingConstants;

public class GameSettings {

    // Turning off ally collision saves a huge number o collision
    private boolean allyCollision;

    public GameSettings() {}

    public boolean isAllyCollision() {
        return allyCollision;
    }
    public void setAllyCollision(boolean allyCollision) {
        this.allyCollision = allyCollision;
    }
}
