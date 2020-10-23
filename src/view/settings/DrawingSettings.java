package view.settings;

public class DrawingSettings {

    // TODO(sonpham): Move this somewhere else. This drawer settings are now doing more than just drawing.
    // TODO(sonpham): Probabaly convert all of this too boolean
    private RenderMode renderMode;
    private DrawingMode drawWeapon;
    private DrawingMode drawEye;
    private int frameSkips;
    private boolean produceFootage;
    private boolean drawSmooth;
    private boolean drawDamageSustained;
    private boolean drawHeightField;
    private boolean drawMapTexture;
    private boolean drawTroopShadow;
    private boolean drawSimplifiedTroopShape;
    private boolean drawIcon;
    private boolean drawTroopInDanger;
    private boolean drawTroopInPosition;
    private boolean drawSurface;
    private boolean drawVideoEffect;
    private boolean drawUnitInfo;
    private boolean drawPathfindingNodes;
    private boolean drawControlArrow;
    private boolean drawGameInfo;
    private boolean drawHitscanLine;

    // Drawing settings for Procedural Content Generation Simulation.
    private boolean showNumAdjacentPolygons;
    private boolean showNumAdjacentEdges;
    private boolean drawPolygonEdges;
    private boolean drawVertices;
    private boolean drawHouses;
    private boolean drawRiver;
    private boolean drawRiverAsCurved;
    private boolean drawTrees;
    private boolean drawRoads;


    // Smooth zoom processing
    private boolean smoothCameraMovement;
    private int smoothRotationSteps;
    private int smoothPlanShowingSteps;

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

    public boolean isDrawHeightField() {
        return drawHeightField;
    }
    public void setDrawHeightField(boolean drawHeightField) {
        this.drawHeightField = drawHeightField;
    }

    public boolean isDrawMapTexture() {
        return drawMapTexture;
    }

    public void setDrawMapTexture(boolean drawMapTexture) {
        this.drawMapTexture = drawMapTexture;
    }

    public boolean isDrawSurface() {
        return drawSurface;
    }

    public void setDrawSurface(boolean drawSurface) {
        this.drawSurface = drawSurface;
    }

    public boolean isDrawTroopShadow() {
        return drawTroopShadow;
    }
    public void setDrawTroopShadow(boolean drawTroopShadow) {
        this.drawTroopShadow = drawTroopShadow;
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

    public boolean isDrawUnitInfo() {
        return drawUnitInfo;
    }
    public void setDrawUnitInfo(boolean drawUnitInfo) {
        this.drawUnitInfo = drawUnitInfo;
    }

    public boolean isDrawPathfindingNodes() {
        return drawPathfindingNodes;
    }
    public void setDrawPathfindingNodes(boolean drawPathfindingNodes) {
        this.drawPathfindingNodes = drawPathfindingNodes;
    }

    public boolean isDrawControlArrow() {
        return drawControlArrow;
    }
    public void setDrawControlArrow(boolean drawControlArrow) {
        this.drawControlArrow = drawControlArrow;
    }

    public boolean isDrawGameInfo() {
        return drawGameInfo;
    }
    public void setDrawGameInfo(boolean drawGameInfo) {
        this.drawGameInfo = drawGameInfo;
    }

    public boolean isShowNumAdjacentPolygons() {
        return showNumAdjacentPolygons;
    }
    public void setShowNumAdjacentPolygons(boolean showNumAdjacentPolygons) {
        this.showNumAdjacentPolygons = showNumAdjacentPolygons;
    }

    public boolean isShowNumAdjacentEdges() {
        return showNumAdjacentEdges;
    }
    public void setShowNumAdjacentEdges(boolean showNumAdjacentEdges) {
        this.showNumAdjacentEdges = showNumAdjacentEdges;
    }

    public boolean isDrawPolygonEdges() {
        return drawPolygonEdges;
    }
    public void setDrawPolygonEdges(boolean drawPolygonEdges) {
        this.drawPolygonEdges = drawPolygonEdges;
    }

    public boolean isDrawVertices() {
        return drawVertices;
    }
    public void setDrawVertices(boolean drawVertices) {
        this.drawVertices = drawVertices;
    }

    public boolean isDrawHouses() {
        return drawHouses;
    }
    public void setDrawHouses(boolean drawHouses) {
        this.drawHouses = drawHouses;
    }

    public boolean isDrawRiver() {
        return drawRiver;
    }
    public void setDrawRiver(boolean drawRiver) {
        this.drawRiver = drawRiver;
    }

    public boolean isDrawRiverAsCurved() {
        return drawRiverAsCurved;
    }
    public void setDrawRiverAsCurved(boolean drawRiverAsCurved) {
        this.drawRiverAsCurved = drawRiverAsCurved;
    }

    public boolean isDrawTrees() {
        return drawTrees;
    }
    public void setDrawTrees(boolean drawTrees) {
        this.drawTrees = drawTrees;
    }

    public boolean isDrawRoads() {
        return drawRoads;
    }
    public void setDrawRoads(boolean drawRoads) {
        this.drawRoads = drawRoads;
    }

    public boolean isDrawHitscanLine() {
        return drawHitscanLine;
    }

    public void setDrawHitscanLine(boolean drawHitscanLine) {
        this.drawHitscanLine = drawHitscanLine;
    }
}
