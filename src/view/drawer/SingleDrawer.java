package view.drawer;

import model.GameEnvironment;
import model.constants.UniversalConstants;
import model.enums.PoliticalFaction;
import model.enums.UnitType;
import model.singles.*;
import model.terrain.Terrain;
import model.utils.MathUtils;
import processing.core.PApplet;
import view.camera.Camera;
import view.camera.CameraConstants;
import view.constants.DrawingConstants;
import view.settings.DrawingMode;
import view.settings.DrawingSettings;

import java.util.HashMap;

public class SingleDrawer extends BaseDrawer {

    // Indexing constants
    private final static int INDEX_TROOP_SIZE = 0;
    private final static int INDEX_SHADOW_SIZE = 1;
    private final static int INDEX_TROOP_SIMPLIED_SIZE = 2;
    private final static int INDEX_SHADOW_SIMPLIED_SIZE = 3;

    // Dependency injections
    PApplet applet;
    Camera camera;
    GameEnvironment env;
    ShapeDrawer shapeDrawer;
    DrawingSettings drawingSettings;

    // Size map storage
    HashMap<Double, Double> eyeSizeMap;
    double currSizeEye;
    HashMap<Double, double[]> swordmanSizeMap;
    double[] currSizeSwordman;
    HashMap<Double, double[]> phalanxSizeMap;
    double[] currSizePhalanx;
    HashMap<Double, double[]> archerSizeMap;
    double[] currSizeArcher;
    HashMap<Double, double[]> balistaSizeMap;
    double[] currSizeBalista;
    HashMap<Double, double[]> slingerSizeMap;
    double[] currSizeSlinger;
    HashMap<Double, double[]> skirmisherSizeMap;
    double[] currSizeSkirmisher;
    HashMap<Double, DrawingVertices> cavalryShapeMap;
    HashMap<Double, double[]> cavalrySizeMap;
    double[] currSizeCavalry;

    double shadowXOffset;
    double shadowYOffset;

    public SingleDrawer(PApplet inputApplet, GameEnvironment inputEnv, Camera inputCamera, ShapeDrawer inputShapeDrawer, DrawingSettings inputDrawingSettings) {

        // Dependency injections
        applet = inputApplet;
        camera = inputCamera;
        shapeDrawer = inputShapeDrawer;
        env = inputEnv;
        drawingSettings = inputDrawingSettings;

        // Graphic storage
        cavalryShapeMap = new HashMap<>();

        // Initialize size map.
        eyeSizeMap = new HashMap<>();
        phalanxSizeMap = new HashMap<>();
        slingerSizeMap = new HashMap<>();
        archerSizeMap = new HashMap<>();
        balistaSizeMap = new HashMap<>();
        skirmisherSizeMap = new HashMap<>();
        swordmanSizeMap = new HashMap<>();
        cavalrySizeMap = new HashMap<>();
    }


    @Override
    public void preprocess() {
        // Pre-calculate shadow, also for optimization
        if (drawingSettings.isDrawTroopShadow()) {
            shadowXOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.SHADOW_OFFSET * camera.getZoom();
            shadowYOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.SHADOW_OFFSET * camera.getZoom();
        }
        
        // Eye
        if (!eyeSizeMap.containsKey(camera.getZoom())) {
            eyeSizeMap.put(camera.getZoom(), UniversalConstants.EYE_SIZE * camera.getZoom());
        }
        currSizeEye = eyeSizeMap.get(camera.getZoom());

        // Swordman
        if (!swordmanSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            double size = env.getGameStats().getSingleStats(UnitType.SWORDMAN, PoliticalFaction.ROME).radius;
            newSize[0] = size * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            swordmanSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeSwordman = swordmanSizeMap.get(camera.getZoom());

        // Phalanx
        if (!phalanxSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            double size = env.getGameStats().getSingleStats(UnitType.PHALANX, PoliticalFaction.ROME).radius;
            newSize[0] = size * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            phalanxSizeMap.put(camera.getZoom(), newSize);
        }
        currSizePhalanx = phalanxSizeMap.get(camera.getZoom());

        // Slinger
        if (!slingerSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            double size = env.getGameStats().getSingleStats(UnitType.SLINGER, PoliticalFaction.ROME).radius;
            newSize[0] = size * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            slingerSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeSlinger = slingerSizeMap.get(camera.getZoom());

        // Archer
        if (!archerSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            double size = env.getGameStats().getSingleStats(UnitType.ARCHER, PoliticalFaction.ROME).radius;
            newSize[0] = size * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            archerSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeArcher = archerSizeMap.get(camera.getZoom());

        // Balista
        if (!balistaSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            double size = env.getGameStats().getSingleStats(UnitType.BALLISTA, PoliticalFaction.ROME).radius;
            newSize[0] = size * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            balistaSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeBalista = balistaSizeMap.get(camera.getZoom());

        // Skirmisher
        if (!skirmisherSizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            double size = env.getGameStats().getSingleStats(UnitType.SKIRMISHER, PoliticalFaction.ROME).radius;
            newSize[0] = size * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            skirmisherSizeMap.put(camera.getZoom(), newSize);
        }
        currSizeSkirmisher = skirmisherSizeMap.get(camera.getZoom());

        // Cavalry
        if (!cavalrySizeMap.containsKey(camera.getZoom())) {
            double[] newSize = new double[4];
            double size = env.getGameStats().getSingleStats(UnitType.CAVALRY, PoliticalFaction.ROME).radius;
            newSize[0] = size * camera.getZoom();
            newSize[1] = newSize[0] * UniversalConstants.SHADOW_SIZE;
            newSize[2] = newSize[0] * UniversalConstants.SIMPLIFIED_SQUARE_SIZE_RATIO;
            newSize[3] = newSize[2] * UniversalConstants.SHADOW_SIZE;
            cavalrySizeMap.put(camera.getZoom(), newSize);
        }
        currSizeCavalry = cavalrySizeMap.get(camera.getZoom());
    }
    /**
     * Dead unit drawer
     * A dead unit will most of the time have just a gray body
     */
    public void drawDeadSingle(BaseSingle single, Terrain terrain) {

        // Recalculate position and shape based on the view.camera position
        double singleX = single.getX();
        double singleY = single.getY();
        double singleZ = terrain.getHeightFromPos(single.getX(), single.getY());
        double[] position = camera.getDrawingPosition(
                singleX,
                singleY,
                singleZ);
        double drawX = position[0];
        double drawY = position[1];
        double zoomAdjustment = camera.getZoomAtHeight(singleZ) / camera.getZoom();

        // Check if the object is drawable. If not, don't portray it.
        if (!DrawingUtils.drawable(drawX, drawY, camera.getWidth(), camera.getHeight())) return;

        // Angle of the dead troop
        double angle = camera.getDrawingAngle(single.getFacingAngle());

        // Fill with color of dead
        int[] color = DrawingConstants.COLOR_DEAD;
        applet.fill(color[0], color[1], color[2], color[3]);

        if (single instanceof CavalrySingle) {
            shapeDrawer.cavalryShape(
                    drawX,
                    drawY,
                    angle,
                    currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment);
        } else if (single instanceof PhalanxSingle) {
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizePhalanx[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment);
        } else if (single instanceof SwordmanSingle) {
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment);
        } else if (single instanceof ArcherSingle) {
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeArcher[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment);
        } else if (single instanceof BallistaSingle) {
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeBalista[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment);
        } else if (single instanceof SlingerSingle) {
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    currSizeSlinger[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeSlinger[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment);
        } else if (single instanceof SkirmisherSingle) {
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeSkirmisher[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment);
        }
    }

    /**
     * Draw alive unit
     */
    public void drawAliveSingle(BaseSingle single, Terrain terrain) {

        // Recalculate position and shape based on the view.camera position
        double singleX = single.getX();
        double singleY = single.getY();
        double singleZ = terrain.getHeightFromPos(single.getX(), single.getY());
        double[] position = camera.getDrawingPosition(singleX, singleY, singleZ);
        double drawX = position[0];
        double drawY = position[1];
        double zoomAdjustment = camera.getZoomAtHeight(singleZ) / camera.getZoom();

        // Check if the object is drawable. If not, don't portray it.
        if (!DrawingUtils.drawable(drawX, drawY, camera.getWidth(), camera.getHeight())) return;

        // Pre calculate drawer information
        double angle = camera.getDrawingAngle(single.getFacingAngle());
        double unitX = MathUtils.quickCos((float) angle);
        double unitY = MathUtils.quickSin((float) angle);

        // Fill the color by political faction
        int[] color = DrawingUtils.getFactionColor(single.getPoliticalFaction());
        int[] modifiedColor = new int[4];
        int[] shadowColor = UniversalConstants.SHADOW_COLOR;

        // Modify the color by the amount of damage sustain
        if (drawingSettings.isDrawDamageSustained()) {
            double sustainColorRatio = Math.min(single.getDamageSustain() / UniversalConstants.DAMAGE_SUSTAIN_MAXIMUM_EFFECT, 1.0);
            modifiedColor[0] = (int) (255.0 * sustainColorRatio + (1 - sustainColorRatio) * color[0]);
            modifiedColor[1] = (int) ((1 - sustainColorRatio) * color[1]);
            modifiedColor[2] = (int) ((1 - sustainColorRatio) * color[2]);
            modifiedColor[3] = 255;
        } else {
            modifiedColor = color;
        }

        // Modify the color if the unit is in danger of being collided
        int index = single.getUnit().getTroopIndex(single);
        if (drawingSettings.isDrawTroopInDanger() &&
                single.getUnit().getInDanger()[index / single.getUnit().getWidth()][index % single.getUnit().getWidth()]) {
            modifiedColor = DrawingConstants.COLOR_IN_DANGER;
        }

        if (drawingSettings.isDrawTroopInPosition() && single.isInPosition()) {
            modifiedColor = DrawingConstants.COLOR_IN_POSITION;
        }

        // Draw the single based on the type of the unit.
        if (single instanceof CavalrySingle) {
            if (drawingSettings.isDrawTroopShadow()) {
                // Calvary shadow
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.cavalryShape(
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                        angle,
                        (currSizeCavalry[INDEX_SHADOW_SIZE] * zoomAdjustment));
            }

            // Calvary shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.cavalryShape(
                    drawX, drawY,
                    angle, (currSizeCavalry[0] * zoomAdjustment));

            // Eye
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.fill(0, 0, 0, 128);
                applet.ellipse((float) (drawX + (unitX * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.25)),
                        (float) (drawY + (unitY * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.25)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Cavalry Sword
            if (drawingSettings.getDrawWeapon() == DrawingMode.DRAW) {
                applet.fill(50, 50, 50);
                double diaRightUnitX = MathUtils.quickCos((float) (angle + Math.PI / 2));
                double diaRightUnitY = MathUtils.quickSin((float) (angle + Math.PI / 2));
                shapeDrawer.sword(
                        (drawX + (diaRightUnitX * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        (drawY + (diaRightUnitY * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        angle,
                        (camera.getZoom() * zoomAdjustment));
            }
        } else if (single instanceof ArcherSingle) {
            // Archer shadow
            if (drawingSettings.isDrawTroopShadow()) {
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape((float) (drawX + shadowXOffset * zoomAdjustment), (float) (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeArcher[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeArcher[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment));
            }

            // Archer circle shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape((float) drawX, (float) drawY,
                    (currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeArcher[INDEX_TROOP_SIMPLIED_SIZE]  * zoomAdjustment));

            // Eye
            applet.fill(0, 0, 0, 128);
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.ellipse((float) (drawX + (unitX * currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (drawY + (unitY * currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Archer bow
            applet.fill(50, 50, 50);
            shapeDrawer.bow((float) drawX, (float) drawY, (float) angle,
                    (float) (currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment));

        } else if (single instanceof BallistaSingle) {
            // Balista shadow
            // TODO: Change the shape to the balista shape
            if (drawingSettings.isDrawTroopShadow()) {
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(
                        (float) (drawX + shadowXOffset * zoomAdjustment),
                        (float) (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeBalista[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeBalista[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment));
            }

            // Balista circle shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape((float) drawX, (float) drawY,
                    (currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeBalista[INDEX_TROOP_SIMPLIED_SIZE]  * zoomAdjustment));

            // Eye
            applet.fill(0, 0, 0, 128);
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.ellipse((float) (drawX + (unitX * currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (drawY + (unitY * currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Archer bow
            applet.fill(50, 50, 50);
            shapeDrawer.bow((float) drawX, (float) drawY, (float) angle,
                    (float) (currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment));

        } else if (single instanceof SkirmisherSingle) {
            // Skirmisher shadow
            if (drawingSettings.isDrawTroopShadow()) {
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(
                        (float) (drawX + shadowXOffset * zoomAdjustment),
                        (float) (drawY + shadowYOffset * zoomAdjustment),
                        (float) (currSizeSkirmisher[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (float) (currSizeSkirmisher[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment));
            }

            // Skirmisher circle shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(
                    (float) drawX,
                    (float) drawY,
                    (float) (currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (float) (currSizeSkirmisher[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment));

            // Eye
            applet.fill(0, 0, 0, 128);
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.ellipse((float) (drawX + (unitX * currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment / 2 * .6)),
                        (float) (drawY + (unitY * currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment / 2 * .6)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }
        } else if (single instanceof SlingerSingle) {
            // Slinger shadow
            if (drawingSettings.isDrawTroopShadow()) {
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape((float) (drawX + shadowXOffset * zoomAdjustment), (float) (drawY + shadowYOffset * zoomAdjustment),
                        (float) (currSizeSlinger[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (float) (currSizeSlinger[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment));
            }

            // Slinger circle shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(
                    (float) drawX,
                    (float) drawY,
                    (float) (currSizeSlinger[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (float) (currSizeSlinger[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment));

            // Eye
            applet.fill(0, 0, 0, 128);
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.ellipse((float) (drawX + (unitX * currSizeSlinger[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (drawY + (unitY * currSizeSlinger[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }
        } else if (single instanceof PhalanxSingle) {
            // Phalanx shadow
            if (drawingSettings.isDrawTroopShadow()) {
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                        (currSizePhalanx[INDEX_SHADOW_SIZE]  * zoomAdjustment),
                        (currSizePhalanx[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment));
            }

            // Phalanx circle shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    (currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizePhalanx[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment));

            // Eye
            applet.fill(0, 0, 0, 128);
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.ellipse((float) (drawX + (unitX * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * .3)),
                        (float) (drawY + (unitY * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * .3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Spear
            if (drawingSettings.getDrawWeapon() == DrawingMode.DRAW) {
                if (camera.getZoom() > CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE || single.getUnit().getTroopIndex(single) / camera.getWidth() < 5) {
                    applet.fill(50, 50, 50);
                    double diaRightUnitX = MathUtils.quickCos((float)(angle + Math.PI / 2));
                    double diaRightUnitY = MathUtils.quickSin((float)(angle + Math.PI / 2));
                    shapeDrawer.spear(
                            (float) (drawX + diaRightUnitX * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45),
                            (float) (drawY + diaRightUnitY * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45),
                            (float) angle, 100,
                            (float) (camera.getZoom() * zoomAdjustment));
                }
            }

        } else if (single instanceof SwordmanSingle) {
            // Swordman shadow
            if (drawingSettings.isDrawTroopShadow()) {
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment));
            }

            // Swordman circle shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    (currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment));

            // Eye
            applet.fill(0, 0, 0, 128);
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.ellipse((float) (drawX + (unitX * currSizeSwordman[0] * zoomAdjustment * .3)),
                        (float) (drawY + (unitY * currSizeSwordman[0] * zoomAdjustment * .3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Sword
            if (drawingSettings.getDrawWeapon() == DrawingMode.DRAW) {
                applet.fill(50, 50, 50);
                double diaRightUnitX = MathUtils.quickCos((float)(angle + Math.PI / 2));
                double diaRightUnitY = MathUtils.quickSin((float)(angle + Math.PI / 2));
                shapeDrawer.sword(
                        (drawX + (diaRightUnitX * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        (drawY + (diaRightUnitY * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        angle,
                        (camera.getZoom() * zoomAdjustment));
            }
        } else {
            // Swordman shadow
            if (drawingSettings.isDrawTroopShadow()) {
                applet.fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment));
            }

            // Swordman circle shape
            applet.fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(
                    drawX,
                    drawY,
                    (currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment));

            // Eye
            applet.fill(0, 0, 0, 128);
            if (drawingSettings.getDrawEye() == DrawingMode.DRAW) {
                applet.ellipse((float) (drawX + (unitX * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment / .3)),
                        (float) (drawY + (unitY * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment / .3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }
        }
    }
}
