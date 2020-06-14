package view.settings;

public class DrawingSettings {

    // TODO(sonpham): Move this somewhere else. This drawer model.settings are now doing more than just drawin.
    // TODO(sonpham): Probabaly convert all of this too boolean
    private RenderMode renderMode;
    private DrawingMode drawWeapon;
    private DrawingMode drawEye;
    private int frameSkips;
    private boolean produceFootage;
    private boolean drawSmooth;
    private boolean drawDamageSustained;
    private boolean drawGrid;
    private boolean drawHeightField;
    private boolean drawTerrainTexture;
    private boolean drawTroopShadow;
    private boolean drawSimplifiedTroopShape;
    private boolean drawIcon;
    private boolean drawTroopInDanger;
    private boolean drawTroopInPosition;
    private boolean drawVideoEffect;

    // Smooth zoom processing
    private boolean smoothCameraMovement;
    private int smoothCameraSteps;
    private int smoothRotationSteps;
    private int smoothPlanShowingSteps;

    // In position optimization means that troop in position will not be drawn separately, but part of a masked image
    // to improve performance
    private boolean inPositionOptimization;

    public DrawingSettings() {}

    public RenderMode getRenderMode() {
        return renderMode;
    }
    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
    }

    public DrawingMode getDrawWeapon() {
        return drawWeapon;
    }
    public void setDrawWeapon(DrawingMode drawWeapon) {
        this.drawWeapon = drawWeapon;
    }

    public DrawingMode getDrawEye() {
        return drawEye;
    }
    public void setDrawEye(DrawingMode drawEye) {
        this.drawEye = drawEye;
    }

    public int getFrameSkips() {
        return frameSkips;
    }
    public void setFrameSkips(int frameSkips) {
        this.frameSkips = frameSkips;
    }

    public boolean isProduceFootage() {
        return produceFootage;
    }
    public void setProduceFootage(boolean produceFootage) {
        this.produceFootage = produceFootage;
    }

    public boolean isSmoothCameraMovement() {
        return smoothCameraMovement;
    }
    public void setSmoothCameraMovement(boolean smoothCameraMovement) {
        this.smoothCameraMovement = smoothCameraMovement;
    }

    public int getSmoothCameraSteps() {
        return smoothCameraSteps;
    }
    public void setSmoothCameraSteps(int smoothCameraSteps) {
        this.smoothCameraSteps = smoothCameraSteps;
    }

    public int getSmoothRotationSteps() {
        return smoothRotationSteps;
    }
    public void setSmoothRotationSteps(int smoothRotationSteps) {
        this.smoothRotationSteps = smoothRotationSteps;
    }

    public boolean isDrawSmooth() {
        return drawSmooth;
    }
    public void setDrawSmooth(boolean drawSmooth) {
        this.drawSmooth = drawSmooth;
    }

    public boolean isDrawDamageSustained() {
        return drawDamageSustained;
    }
    public void setDrawDamageSustained(boolean drawDamageSustained) {
        this.drawDamageSustained = drawDamageSustained;
    }

    public boolean isDrawGrid() {
        return drawGrid;
    }
    public void setDrawGrid(boolean drawGrid) {
        this.drawGrid = drawGrid;
    }

    public boolean isDrawHeightField() {
        return drawHeightField;
    }
    public void setDrawHeightField(boolean drawHeightField) {
        this.drawHeightField = drawHeightField;
    }

    public boolean isDrawTerrainTexture() {
        return drawTerrainTexture;
    }
    public void setDrawTerrainTexture(boolean drawTerrainTexture) {
        this.drawTerrainTexture = drawTerrainTexture;
    }

    public boolean isDrawTroopShadow() {
        return drawTroopShadow;
    }
    public void setDrawTroopShadow(boolean drawTroopShadow) {
        this.drawTroopShadow = drawTroopShadow;
    }

    public boolean isInPositionOptimization() {
        return inPositionOptimization;
    }
    public void setInPositionOptimization(boolean inPositionOptimization) {
        this.inPositionOptimization = inPositionOptimization;
    }

    public boolean isDrawIcon() {
        return drawIcon;
    }
    public void setDrawIcon(boolean drawIcon) {
        this.drawIcon = drawIcon;
    }

    public boolean isDrawSimplifiedTroopShape() {
        return drawSimplifiedTroopShape;
    }

    public void setDrawSimplifiedTroopShape(boolean drawSimplifiedTroopShape) {
        this.drawSimplifiedTroopShape = drawSimplifiedTroopShape;
    }

    public int getSmoothPlanShowingSteps() {
        return smoothPlanShowingSteps;
    }
    public void setSmoothPlanShowingSteps(int smoothPlanShowingSteps) {
        this.smoothPlanShowingSteps = smoothPlanShowingSteps;
    }

    public boolean isDrawVideoEffect() {
        return drawVideoEffect;
    }

    public void setDrawVideoEffect(boolean drawVideoEffect) {
        this.drawVideoEffect = drawVideoEffect;
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
