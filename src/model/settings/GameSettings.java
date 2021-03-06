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

    // Count the number of erroneous formation changes.
    private boolean countWrongFormationChanges;

    // If true, we will round down the surface data into grids of binary collision, which makes the collision checking
    // between troops and surface a lot faster but inaccurate.
    private boolean useRoundedSurfaceCollision;

    // Intention skipping settings
    private int unitIntentionSkipping;
    private int singleIntentionSkipping;

    // Process unit visions.
    private boolean processUnitVision;

    // Sound bouncing processing setting. Currently an experimental feature that is false by default.
    // Very heavy on processing.
    // TODO: Add some logging for sound bouncing processing.
    private boolean processSoundBounce;

    // Create AI Agents
    private boolean createAIAgent;

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

    public boolean isCountWrongFormationChanges() {
        return countWrongFormationChanges;
    }
    public void setCountWrongFormationChanges(boolean countWrongFormationChanges) {
        this.countWrongFormationChanges = countWrongFormationChanges;
    }

    public int getUnitIntentionSkipping() {
        return unitIntentionSkipping;
    }
    public void setUnitIntentionSkipping(int unitIntentionSkipping) {
        this.unitIntentionSkipping = unitIntentionSkipping;
    }

    public int getSingleIntentionSkipping() {
        return singleIntentionSkipping;
    }
    public void setSingleIntentionSkipping(int singleIntentionSkipping) {
        this.singleIntentionSkipping = singleIntentionSkipping;
    }

    public boolean isProcessSoundBounce() {
        return processSoundBounce;
    }
    public void setProcessSoundBounce(boolean processSoundBounce) {
        this.processSoundBounce = processSoundBounce;
    }

    public boolean isUseRoundedSurfaceCollision() {
        return useRoundedSurfaceCollision;
    }
    public void setUseRoundedSurfaceCollision(boolean useRoundedSurfaceCollision) {
        this.useRoundedSurfaceCollision = useRoundedSurfaceCollision;
    }

    public boolean isProcessUnitVision() {
        return processUnitVision;
    }
    public void setProcessUnitVision(boolean processUnitVision) {
        this.processUnitVision = processUnitVision;
    }

    public boolean isCreateAIAgent() {
        return createAIAgent;
    }
    public void setCreateAIAgent(boolean createAIAgent) {
        this.createAIAgent = createAIAgent;
    }
}