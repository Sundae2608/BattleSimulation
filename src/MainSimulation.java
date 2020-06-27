import cern.colt.function.tint.IntIntFunction;
import cern.colt.matrix.tint.IntMatrix2D;
import cern.colt.matrix.tint.impl.DenseIntMatrix2D;
import controller.ControlConstants;
import model.checker.EnvironmentChecker;
import model.construct.Construct;
import model.enums.*;
import model.objects.Ballista;
import model.objects.Stone;
import model.settings.GameSettings;
import model.surface.BaseSurface;
import model.surface.ForestSurface;
import model.surface.Tree;
import model.terrain.Terrain;
import org.opencv.core.Core;
import utils.ConfigUtils;
import view.audio.AudioConstants;
import view.audio.AudioSpeaker;
import view.audio.AudioType;
import view.camera.CameraConstants;
import view.drawer.UIDrawer;
import view.drawer.ShapeDrawer;
import javafx.util.Pair;
import view.map.Tile;
import model.GameEnvironment;
import view.camera.Camera;
import model.constants.*;
import model.objects.Arrow;
import model.objects.BaseObject;
import processing.core.PGraphics;
import view.drawer.DrawingVertices;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;
import processing.sound.SoundFile;
import model.singles.*;
import model.units.*;
import model.utils.*;
import view.constants.DrawingConstants;
import view.settings.AudioSettings;
import view.settings.DrawingMode;
import view.settings.DrawingSettings;
import view.settings.RenderMode;
import view.terrain.TerrainDrawer;
import view.utils.DrawingUtils;
import view.video.VideoElementPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainSimulation extends PApplet {

    // -------------------
    // Universal Constants
    // -------------------
    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;
    // ------------------
    // Drawers
    // This helps store each special shape at size to save time.
    // TODO: Wrap around this into an optimizer object for scalability
    // ------------------

    // All Drawers
    UIDrawer uiDrawer;
    ShapeDrawer shapeDrawer;
    TerrainDrawer terrainDrawer;

    // Eye optimizer
    HashMap<Double, Double> eyeSizeMap;
    double currSizeEye;

    // Unit optimizers
    private final static int INDEX_TROOP_SIZE = 0;
    private final static int INDEX_SHADOW_SIZE = 1;
    private final static int INDEX_TROOP_SIMPLIED_SIZE = 2;
    private final static int INDEX_SHADOW_SIMPLIED_SIZE = 3;

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

    HashMap<BaseUnit, Pair<PImage, IntMatrix2D>> unitSimplifiedImageMap;
    HashMap<BaseUnit, Pair<PImage, IntMatrix2D>> unitShadowSimplifiedImageMap;

    double shadowXOffset;
    double shadowYOffset;
    double unitShadowXOffset;
    double unitShadowYOffset;

    // -----------
    // Sound files
    // -----------

    AudioSpeaker audioSpeaker;
    SoundFile backgroundMusic;

    // --------------------
    // Video element player
    // --------------------
    VideoElementPlayer videoElementPlayer;

    // -----------
    // Image files
    // -----------

    // UI Icons
    // TODO(sonpham): Change this to a UnitType to icon model.
    PImage iconCav, iconSpear, iconSword, iconArcher, iconSlinger, iconHorseArcher, iconSkirmisher, iconBallista, iconCatapult;
    PImage banner, bannerShadow, bannerSelected, bannerTexture;

    // Tiles
    // TODO(sonpham): This is deprecated
    PImage tileGrass;

    // List of all tiles
    private ArrayList<Tile> tiles;

    // --------------------
    // Game variables
    // Variable necessary for the running of the game
    // --------------------
    GameSettings gameSettings;
    GameEnvironment env;

    // Camera
    Camera camera;

    // Some drawingSettings
    DrawingSettings drawingSettings;
    AudioSettings audioSettings;
    int zoomCounter;
    double zoomGoal;
    int angleCounter;
    double angleGoal;
    int planCounter;

    // Time recorder
    long lastTime;
    long backEndTime;
    long graphicTime;

    // Current playing state
    boolean currentlyPaused;

    // Control variable
    BaseUnit unitSelected;
    boolean rightClickedNotReleased;
    double rightClickActualX;
    double rightClickActualY;
    double unitEndPointX;
    double unitEndPointY;
    double unitEndAngle;
    BaseUnit closestUnit;


    public void settings() {

        // Window size
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);

        // -------------
        // Game settings
        // -------------
        gameSettings = new GameSettings();
        gameSettings.setApplyTerrainModifier(true);
        gameSettings.setBorderInwardCollision(false);
        gameSettings.setAllyCollision(true);
        gameSettings.setCollisionCheckingOnlyInCombat(false);
        gameSettings.setCavalryCollision(true);

        // ----------------
        // Graphic settings
        // ----------------
        drawingSettings = new DrawingSettings();
        drawingSettings.setRenderMode(RenderMode.MINIMALISTIC);
        drawingSettings.setDrawEye(DrawingMode.NOT_DRAW);
        drawingSettings.setDrawWeapon(DrawingMode.DRAW);
        drawingSettings.setProduceFootage(false);
        drawingSettings.setFrameSkips(0);
        drawingSettings.setDrawGrid(false);
        drawingSettings.setSmoothCameraMovement(true);
        drawingSettings.setSmoothCameraSteps(100);
        drawingSettings.setSmoothRotationSteps(40);
        drawingSettings.setSmoothPlanShowingSteps(100);
        drawingSettings.setDrawHeightField(true);
        drawingSettings.setDrawTerrainTexture(false);
        drawingSettings.setDrawSmooth(true);
        drawingSettings.setDrawDamageSustained(true);
        drawingSettings.setDrawTroopShadow(true);
        drawingSettings.setDrawSimplifiedTroopShape(true);
        drawingSettings.setDrawIcon(true);
        drawingSettings.setInPositionOptimization(false);
        drawingSettings.setDrawVideoEffect(true);

        // Initialize drawer
        uiDrawer = new UIDrawer();
        shapeDrawer = new ShapeDrawer();

        // Graphic storage
        cavalryShapeMap = new HashMap<>();

        eyeSizeMap = new HashMap<>();
        phalanxSizeMap = new HashMap<>();
        slingerSizeMap = new HashMap<>();
        archerSizeMap = new HashMap<>();
        balistaSizeMap = new HashMap<>();
        skirmisherSizeMap = new HashMap<>();
        swordmanSizeMap = new HashMap<>();
        cavalrySizeMap = new HashMap<>();

        unitSimplifiedImageMap = new HashMap<>();
        unitShadowSimplifiedImageMap = new HashMap<>();

        // --------------
        // Audio model.settings
        // --------------
        audioSettings = new AudioSettings();
        audioSettings.setBackgroundMusic(false);
        audioSettings.setSoundEffect(true);

        // ------------------------
        // Post processing model.settings
        // ------------------------
        if (!drawingSettings.isDrawSmooth()) noSmooth();
        else {
            smooth(4);
        }
    }

    public void setup() {

        // ----------------------
        // Load graphic resources
        // ----------------------
        iconSword = loadImage("imgs/BannerArt/iconSword.png");
        iconSpear = loadImage("imgs/BannerArt/iconSpear.png");
        iconCav = loadImage("imgs/BannerArt/iconCav.png");
        iconArcher = loadImage("imgs/BannerArt/iconArcher.png");
        iconHorseArcher = loadImage("imgs/BannerArt/iconHorseArcher.png");
        iconSlinger = loadImage("imgs/BannerArt/iconSlinger.png");
        iconSkirmisher = loadImage("imgs/BannerArt/iconSkirmisher.png");
        iconBallista = loadImage("imgs/BannerArt/iconBallista.png");
        iconCatapult = loadImage("imgs/BannerArt/iconCatapult.png");

        banner = loadImage("imgs/BannerArt/SimplifiedBanner-01.png");
        bannerShadow = loadImage("imgs/BannerArt/SimplifiedBanner-02.png");
        bannerTexture = loadImage("imgs/BannerArt/SimplifiedBanner-03.png");
        bannerSelected = loadImage("imgs/BannerArt/SimplifiedBanner-04.png");

        tileGrass = loadImage("imgs/SelectedTiles/grassTile128.png");

        // -------------------
        // Preprocesing troops
        // -------------------

        // Create a new game based on the input configurations.
        String battleConfig = "src/configs/battle_configs/CavVsSwordmen.txt";
        String mapConfig = "src/configs/map_configs/MapConfig.txt";
        String constructsConfig = "src/configs/construct_configs/ConstructsMapConfig.txt";
        String surfaceConfig = "src/configs/surface_configs/SurfaceConfig.txt";
        String gameConfig = "src/configs/game_configs/GameConfig.txt";
        env = new GameEnvironment(gameConfig, mapConfig, constructsConfig, surfaceConfig, battleConfig, gameSettings);

        // Check to make sure that the game environment is valid
        try {
            EnvironmentChecker.checkEnvironmentValid(env);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ------
        // Camera
        // ------
        // Add unit to view.camera
        camera = new Camera(INPUT_WIDTH / 2, INPUT_HEIGHT / 2, INPUT_WIDTH, INPUT_HEIGHT,
                env.getBroadcaster());
        zoomGoal = camera.getZoom();  // To ensure consistency
        angleGoal = camera.getAngle();

        if (drawingSettings.isDrawTerrainTexture()) {
            PImage[][] images = ConfigUtils.createTerrainTilesFromConfig(
                    "imgs/MapTiles/pharsalus", this);
            try {
                terrainDrawer = new TerrainDrawer(env.getTerrain(), images, camera, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // -------------------------
        // Load video element player
        // -------------------------
        try {
            videoElementPlayer = ConfigUtils.readVideoElementConfig(
                    "src/configs/graphic_configs/GraphicConfig.txt",
                    camera, this, env.getBroadcaster()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ---------------
        // Load sound file
        // ---------------
        try {
            audioSpeaker = ConfigUtils.readAudioConfigs(
                    "src/configs/audio_configs/AudioConfig.txt",
                    camera, this, env.getBroadcaster()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load background music
        if (audioSettings.isBackgroundMusic()) {
            backgroundMusic = new SoundFile(this, "audios/bg_music/bg1.mp3");
            backgroundMusic.amp(0.15f);
            backgroundMusic.loop();
        }

        // Create a simplified image version of each unit.
        preprocessSimplifiedUnitImages();

        // Playing state
        currentlyPaused = false;

        // Some graphic set up
        rectMode(CENTER);
    }

    /**
     * Looping method required for a Processing Applet.
     */
    public void draw() {

        // -------------------
        // Update the back end
        // -------------------

        // Record time
        lastTime = System.currentTimeMillis();

        if (!currentlyPaused) {
            // The environment makes one step forward in processing.
            env.step();
            camera.update();
        }

        // Record backend time
        backEndTime = System.currentTimeMillis() - lastTime;

        // ----------------------------------------
        // Update some graphical elements
        // Some graphic elements of the game is extracted and pre-processed here.
        // - Camera position updates.
        // - Smooth zoom update (if configured)
        // - Circle Size optimization
        // - Update nearest unit to mouse cursor
        // ----------------------------------------

        // Frame skipper
        if (drawingSettings.getFrameSkips() != 0 & frameCount % (drawingSettings.getFrameSkips() + 1) == 0) return;

        // Update view.camera smooth zoom
        if (drawingSettings.isSmoothCameraMovement()) {
            if (zoomCounter >= 0) {
                zoomCounter -= 1;
                double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / drawingSettings.getSmoothCameraSteps();
                camera.setZoom(zoom);
            }
            if (keyPressed) {
                if (key == 'q') camera.setAngle(camera.getAngle() + CameraConstants.CAMERA_ROTATION_SPEED);
                if (key == 'e') camera.setAngle(camera.getAngle() - CameraConstants.CAMERA_ROTATION_SPEED);
            }
        }

        // Update view.camera keyboard movement
        double screenMoveAngle;
        if (keyPressed) {
            if (key == 'a') {
                screenMoveAngle = Math.PI + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(CameraConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        CameraConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
            if (key == 'd') {
                screenMoveAngle = camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(CameraConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        CameraConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
            if (key == 'w') {
                screenMoveAngle = Math.PI * 3 / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(CameraConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        CameraConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
            if (key == 's') {
                screenMoveAngle = Math.PI / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                camera.move(CameraConstants.CAMERA_SPEED / camera.getZoom() * unitX,
                        CameraConstants.CAMERA_SPEED / camera.getZoom() * unitY);
            }
        }

        // Circle size optimization. This is to avoid repeat calculation of troop size
        updateSizeMaps();

        // Pre-calculate shadow, also for optimization
        if (drawingSettings.isDrawTroopShadow()) {
            shadowXOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.SHADOW_OFFSET * camera.getZoom();
            shadowYOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.SHADOW_OFFSET * camera.getZoom();
            unitShadowXOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
            unitShadowYOffset = MathUtils.quickCos((float) UniversalConstants.SHADOW_ANGLE) * UniversalConstants.UNIT_SHADOW_OFFSET * camera.getZoom();
        }

        // Pick the closest to the mouse
        double[] mousePositions = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
        double minDist = Double.MAX_VALUE;
        closestUnit = env.getUnits().get(0);
        for (BaseUnit unit : env.getUnits()) {
            double dist = MathUtils.squareDistance(unit.getAverageX(),
                    unit.getAverageY(),
                    mousePositions[0],
                    mousePositions[1]);
            if (dist < minDist) {
                minDist = dist;
                closestUnit = unit;
            }
        }

        // -----------------
        // Draw the graphics
        // -----------------

        // Clear everything
        background(230);

        // Then, draw the map texture
        if (drawingSettings.isDrawTerrainTexture()) {
            terrainDrawer.drawTerrain();
        }

        // Then, draw the dots that represents the height.
        if (drawingSettings.isDrawHeightField()) {
            drawTerrainLine(env.getTerrain(), camera);
        }

        // Begin loop for columns
        if (drawingSettings.isDrawGrid()) {
            double[] drawPos = camera.getDrawingPosition(0.0, 0.0);
            double drawX = drawPos[0];
            double drawY = drawPos[1];
            float spacing = (float) (DrawingConstants.GRID_SIZE * (camera.getZoom()));

            float[] color = DrawingConstants.GRID_COLOR;
            stroke(color[0], color[1], color[2], color[3]);
            strokeWeight(1);

            for (float i = (float) drawX % spacing - spacing; i <= width; i += spacing) {
                line(i, 0, i, height);
            }
            for (float j = (float) drawY % spacing - spacing; j <= height; j += spacing) {
                line(0, j, width, j);
            }
            noStroke();
        }

        // Draw the grass tile.
        if (drawingSettings.getRenderMode() == RenderMode.REALISTIC) {
            double[] drawPos = camera.getDrawingPosition(0.0, 0.0);
            double drawX = drawPos[0];
            double drawY = drawPos[1];
            float drawWidth = (float) (tileGrass.width * (camera.getZoom()));
            float drawHeight = (float) (tileGrass.height * (camera.getZoom()));
            for (float i = (float) drawX % drawWidth - drawWidth; i <= width ; i += drawWidth) {
                for (float j = (float) drawY % drawHeight - drawHeight; j <= height; j += drawHeight) {
                    image(tileGrass, i, j, drawWidth, drawHeight);
                }
            }
        }

        // Draw the surface.
        for (BaseSurface surface : env.getSurfaces()) {
            int[] surfaceColor = DrawingUtils.getSurfaceColor(surface);
            fill(surfaceColor[0], surfaceColor[1], surfaceColor[2], surfaceColor[3]);
            double[][] pts = surface.getSurfaceBoundary();
            beginShape();
            for (int i = 0; i < pts.length; i++) {
                // TODO: This is an efficient part, the height of the object is recalculated all the time.
                double[] drawingPts = camera.getDrawingPosition(pts[i][0], pts[i][1],
                        env.getTerrain().getHeightFromPos(pts[i][0], pts[i][1]));
                vertex((float) drawingPts[0], (float) drawingPts[1]);
            }
            endShape(CLOSE);

            if (surface.getType() == SurfaceType.FOREST) {
                for (Tree tree : ((ForestSurface) surface).getTrees()) {
                    int[] treeColor = DrawingConstants.TREE_COLOR;
                    double height = env.getTerrain().getHeightFromPos(tree.getX(), tree.getY());
                    fill(treeColor[0], treeColor[1], treeColor[2], treeColor[3]);
                    double[] drawingPosition = camera.getDrawingPosition(tree.getX(), tree.getY(),
                            height);
                    circle((float) drawingPosition[0], (float) drawingPosition[1],
                            (float) (tree.getRadius() * 2 * camera.getZoomAtHeight(height)));
                }
            }
        }

        // Dead troops
        noStroke();
        for (BaseSingle single : env.getDeadContainer()) {
            portrayDeadSingle(single, camera, env.getTerrain(), drawingSettings);
        }

        // If space is pressed, draw the goal position.
        if (keyPressed) {
            if (key == TAB) {
                planCounter = drawingSettings.getSmoothPlanShowingSteps();
            }
        }

        if (planCounter > 0) {
            for (BaseUnit unit : env.getUnits()) {
                if (unit.getAliveTroopsSet().size() == 0) break;
                if (unit.getState() == UnitState.MOVING) {
                    if (unit == unitSelected) continue;
                    int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
                    fill(color[0], color[1], color[2], (int) (Math.min(1.0 * planCounter / 30, 0.90) * 255));
                    drawArrowPlan(
                            unit.getAverageX(), unit.getAverageY(),
                            unit.getGoalX(), unit.getGoalY(),
                            camera, drawingSettings);
                }
            }
            planCounter -= 1;
        }

        // Always draw arrow of selected unit
        // TODO(sonpham) Refactor this into unit graphics
        if (unitSelected != null) {

            // Get the political faction color
            int[] color = DrawingUtils.getFactionColor(unitSelected.getPoliticalFaction());

            // If the mouse is being right clicked, the user can drag to select whether the goal would be. Visualize
            // the changed formation if the new width of the front makes sense.
            double[] actualCurrent = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
            double distance = MathUtils.quickDistance(
                    rightClickActualX, rightClickActualY, actualCurrent[0], actualCurrent[1]);
            double drawingEndPointX;
            double drawingEndPointY;
            if (rightClickedNotReleased && distance >
                    ControlConstants.MINIMUM_WIDTH_SELECTION * unitSelected.getUnitStats().spacing) {
                // Draw the rectangle showing unit formation selection
                // TODO: Convert to the actual game angle, and always anchor on the actual angle on the field would be
                //  the best way to ensure acccurate angle calculation.
                double[] actualMouseClicked = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                double angle = MathUtils.atan2(
                        actualMouseClicked[1] - rightClickActualY,
                        actualMouseClicked[0] - rightClickActualX);
                double sideUnitX = MathUtils.quickCos((float) angle);
                double sideUnitY = MathUtils.quickSin((float) angle);
                double downUnitX = MathUtils.quickCos((float) (angle + Math.PI / 2));
                double downUnitY = MathUtils.quickSin((float) (angle + Math.PI / 2));
                double frontlineWidth = Math.min((int) (
                        distance / unitSelected.getUnitStats().spacing),
                        unitSelected.getNumAlives()) * unitSelected.getUnitStats().spacing;
                double depthDistance = unitSelected.getNumAlives() * unitSelected.getUnitStats().spacing *
                        unitSelected.getUnitStats().spacing / frontlineWidth;
                fill(color[0], color[1], color[2], DrawingConstants.COLOR_ALPHA_UNIT_SELECTION);

                // Formation shape
                double[] pts1 = camera.getDrawingPosition(rightClickActualX, rightClickActualY);
                double[] pts2 = camera.getDrawingPosition(actualMouseClicked[0], actualMouseClicked[1]);
                double[] pts3 = camera.getDrawingPosition(
                        actualMouseClicked[0] + depthDistance * downUnitX,
                        actualMouseClicked[1] + depthDistance * downUnitY);
                double[] pts4 = camera.getDrawingPosition(
                        rightClickActualX + depthDistance * downUnitX,
                        rightClickActualY + depthDistance * downUnitY);

                beginShape();
                vertex((float) pts1[0], (float) pts1[1]);
                vertex((float) pts2[0], (float) pts2[1]);
                vertex((float) pts3[0], (float) pts3[1]);
                vertex((float) pts4[0], (float) pts4[1]);
                endShape(CLOSE);

                // Modify end point for the unit.
                unitEndPointX = rightClickActualX + frontlineWidth * sideUnitX / 2;
                unitEndPointY = rightClickActualY + frontlineWidth * sideUnitY / 2;
                drawingEndPointX = unitEndPointX;
                drawingEndPointY = unitEndPointY;
                unitEndAngle = angle - Math.PI / 2;
            } else {
                double[] endPoints = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                drawingEndPointX = endPoints[0];
                drawingEndPointY = endPoints[1];
                unitEndPointX = endPoints[0];
                unitEndPointY = endPoints[1];
                unitEndAngle = MathUtils.atan2(endPoints[1] - unitSelected.getAnchorY(), endPoints[0] - unitSelected.getAnchorX());
            }

            // Draw the arrow plan if the unit is current moving.
            if (unitSelected.getState() == UnitState.MOVING) {
                fill(color[0], color[1], color[2], 255);
                drawArrowPlan(
                        unitSelected.getAverageX(), unitSelected.getAverageY(),
                        unitSelected.getGoalX(), unitSelected.getGoalY(), camera, drawingSettings);
            }

            // Intention arrow
            fill(color[0], color[1], color[2], 128);
            if ((unitSelected instanceof ArcherUnit ||
                unitSelected instanceof BallistaUnit ||
                unitSelected instanceof CatapultUnit) && closestUnit.getPoliticalFaction() != unitSelected.getPoliticalFaction()) {
                drawArrowPlan(
                        unitSelected.getAverageX(), unitSelected.getAverageY(),
                        closestUnit.getAverageX(), closestUnit.getAverageY(), camera, drawingSettings);
            } else {
                drawArrowPlan(
                        unitSelected.getAverageX(), unitSelected.getAverageY(),
                        drawingEndPointX, drawingEndPointY, camera, drawingSettings);
            }
        }

        if (camera.getZoom() > CameraConstants.ZOOM_RENDER_LEVEL_TROOP) {
            // Alive troop
            for (BaseUnit unit : env.getUnits()) {
                boolean unitHovered = unit == closestUnit;
                // First, draw the optimize masked version for troops in position
                if (drawingSettings.isInPositionOptimization() &&
                        camera.getZoom() < CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE) {
                    drawMaskedInPositionUnit(unit, camera, drawingSettings);
                }
                // For troops out of position, draw them individually
                for (BaseSingle single : unit.getTroops()) {
                    if (single.getState() == SingleState.DEAD) continue;
                    portrayAliveSingle(single, camera, drawingSettings, env.getTerrain(), unitHovered);
                }
            }
        } else {
            // Draw unit block
            for (BaseUnit unit : env.getUnits()) {
                // TODO: Change to using UnitState instead for consistency
                if (unit.getNumAlives() == 0) continue;
                drawUnitBlock(unit, camera, drawingSettings, false);
            }
        }

        // Draw the arrow direction of the unit
        for (BaseUnit unit : env.getUnits()) {
            double unitX = MathUtils.quickCos((float) unit.getAnchorAngle());
            double unitY = MathUtils.quickSin((float) unit.getAnchorAngle());
            int[] color = DrawingConstants.COLOR_GOOD_BLACK;
            fill(color[0], color[1], color[2], color[3]);
            drawArrowPlanAtHeight(
                    unit.getAnchorX(), unit.getAnchorY(),
                    unit.getAnchorX() + unitX * DrawingConstants.ANCHOR_ARROW_SIZE,
                    unit.getAnchorY() + unitY * DrawingConstants.ANCHOR_ARROW_SIZE,
                    env.getTerrain().getHeightFromPos(unit.getAnchorX(), unit.getAnchorY()),
                    camera, drawingSettings);
        }

        // Draw the objects
        ArrayList<BaseObject> objects = env.getUnitModifier().getObjectHasher().getObjects();
        for (BaseObject obj : objects) {
            if (obj.isAlive()) drawObject(obj, camera, env.getTerrain(), drawingSettings);
        }

        // Draw the construct.
        for (Construct construct : env.getConstructs()) {
            int[] constructColor = DrawingConstants.COLOR_GOOD_BLACK;
            fill(constructColor[0], constructColor[1], constructColor[2]);
            double[][] pts = construct.getBoundaryPoints();
            beginShape();
            for (int i = 0; i < pts.length; i++) {
                // TODO: This is an efficient part, the height of the object is recalculated all the time.
                double[] drawingPts = camera.getDrawingPosition(pts[i][0], pts[i][1],
                        env.getTerrain().getHeightFromPos(pts[i][0], pts[i][1]));
                vertex((float) drawingPts[0], (float) drawingPts[1]);
            }
            endShape(CLOSE);
        }

        if (drawingSettings.isDrawVideoEffect()) videoElementPlayer.processElementQueue();

        // -------------------
        // Procecss unit sound
        // -------------------
        if (audioSettings.isSoundEffect()) audioSpeaker.processEvents();

        // -----------
        // Draw the UI
        // -----------

        // Draw all the unit icon
        ArrayList<BaseUnit> unitsSortedByPosition = (ArrayList) env.getUnits().clone();
        Collections.sort(unitsSortedByPosition, new Comparator<BaseUnit>() {
            @Override
            public int compare(BaseUnit o1, BaseUnit o2) {
                double y1 = camera.getDrawingPosition(o1.getAverageX(), o1.getAverageY())[1];
                double y2 = camera.getDrawingPosition(o2.getAverageX(), o2.getAverageY())[1];
                return Double.compare(y1, y2);
            }
        });
        if (drawingSettings.isDrawIcon()) {
            for (BaseUnit unit : unitsSortedByPosition) {
                if (unit.getNumAlives() == 0) continue;
                double[] drawingPos = camera.getDrawingPosition(unit.getAverageX(), unit.getAverageY(), unit.getAverageZ());
                int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
                rectMode(CORNER);
                imageMode(CORNER);
                blendMode(NORMAL);
                image(unit == unitSelected ? bannerSelected : bannerShadow,
                        (float) (drawingPos[0] - 42),
                        (float) (drawingPos[1] - 111), 84, 111);
                fill(color[0], color[1], color[2], 255);
                rect(
                        (float) (drawingPos[0] - 30),
                        (float) (drawingPos[1] - 93 + 60.0 * (1.0 - 1.0 * unit.getNumAlives() / unit.getTroops().size())),
                        (float) 60, (float) (60.0 * unit.getNumAlives() / unit.getTroops().size()));
                image(banner,
                        (float) (drawingPos[0] - 42),
                        (float) (drawingPos[1] - 111), 84, 111);
                int[] moraleColor = DrawingConstants.COLOR_MORALE;
                fill(moraleColor[0], moraleColor[1], moraleColor[2], moraleColor[3]);
                rect((float) (drawingPos[0] - 28), (float) (drawingPos[1] - 32),
                        (float) (56 * unit.getMorale() / GameplayConstants.BASE_MORALE), 8);
                blendMode(MULTIPLY);
                image(bannerTexture,
                        (float) (drawingPos[0] - 42),
                        (float) (drawingPos[1] - 111), 84, 111);
                blendMode(NORMAL);
                if (unit instanceof CavalryUnit) {
                    image(iconCav,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 92, 60, 60);
                } else if (unit instanceof PhalanxUnit) {
                    image(iconSpear,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 92, 60, 60);
                } else if (unit instanceof SwordmenUnit) {
                    image(iconSword,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 92, 60, 60);
                } else if (unit instanceof ArcherUnit) {
                    image(iconArcher,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 92, 60, 60);
                } else if (unit instanceof SkirmisherUnit) {
                    image(iconSkirmisher,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 92, 60, 60);
                } else if (unit instanceof BallistaUnit) {
                    image(iconBallista,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 92, 60, 60);
                } else if (unit instanceof CatapultUnit) {
                    image(iconCatapult,
                            (float) drawingPos[0] - 30,
                            (float) drawingPos[1] - 92, 60, 60);
                }
            }
        }
        rectMode(CENTER);

        // Information about the unit
        fill(0, 0, 0, 200);
        rect(0, 0, 400, 150);
        fill(255, 255, 255);
        textAlign(LEFT);
        text(UnitUtils.getUnitName(closestUnit), 8, 15);
        text("Unit state: ", 8, 35); text(closestUnit.getState().toString(), 100, 35);
        text("Strength: ", 8, 50); text(String.valueOf(closestUnit.getNumAlives()) + "/" + String.valueOf(closestUnit.getTroops().size()), 100, 50);

        // Process graphics
        fill(0, 0, 0);
        graphicTime = System.currentTimeMillis() - lastTime - backEndTime;
        textAlign(LEFT);
        text("Camera shake level: " + Double.toString(camera.getCameraShakeLevel()), 5, INPUT_HEIGHT - 65);
        text("Zoom level: " + Double.toString(camera.getZoom()), 5, INPUT_HEIGHT - 50);
        text("Backends: " + Long.toString(backEndTime) + "ms", 5, INPUT_HEIGHT - 35);
        text("Graphics: " + Long.toString(graphicTime) + "ms", 5, INPUT_HEIGHT - 20);
        if (graphicTime + backEndTime != 0) {
            text("FPS: " + Long.toString(1000 / (graphicTime + backEndTime)), 5, INPUT_HEIGHT - 5);
        } else {
            text("FPS: Infinity", 5, INPUT_HEIGHT - 5);
        }

        // Pause / Play Button
        if (!currentlyPaused) {
            uiDrawer.pauseButton(g,INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        } else {
            uiDrawer.playButton(g,INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        }

        // -----------------
        // Produce the image
        // -----------------
        if (drawingSettings.isProduceFootage()) {
            // Saves each frame as line-000001.png, line-000002.png, etc.
            saveFrame("line-######.png");
        }
    }

    /**
     * Check if mouse position is in the range of the pause or play button
     */
    private boolean overPauseOrPlay() {
        return (INPUT_WIDTH - 70 < mouseX && mouseX < INPUT_WIDTH - 30 &&
                INPUT_HEIGHT - 70 < mouseY && mouseY < INPUT_HEIGHT - 30);
    }

    /**
     * Process the mouse clicked.
     */
    public void mousePressed() {
        if (mouseButton == RIGHT) {
            rightClickedNotReleased = true;
            double[] actualRightClicked = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
            rightClickActualX = actualRightClicked[0];
            rightClickActualY = actualRightClicked[1];
        }
    }

    @Override
    /**
     * Process the mouse released
     */
    public void mouseReleased() {

        // Turn off the drag and drop functionality.
        rightClickedNotReleased = false;

        // Check if in the range of pause button
        if (overPauseOrPlay()) {
            if(currentlyPaused) {
                currentlyPaused = false;
                if (audioSettings.isBackgroundMusic()) backgroundMusic.loop();
                if (audioSettings.isSoundEffect()) {
                    audioSpeaker.resumeAllAmbientSounds();
                }
            } else {
                currentlyPaused = true;
                if (audioSettings.isBackgroundMusic()) backgroundMusic.pause();
                if (audioSettings.isSoundEffect()) {
                    audioSpeaker.pauseAllAmbientSounds();
                }
            }
        }

        //
        if (mouseButton == LEFT) {
            unitSelected = closestUnit;
            audioSpeaker.broadcastOverlaySound(AudioType.LEFT_CLICK);
        } else if (mouseButton == RIGHT) {
            audioSpeaker.broadcastOverlaySound(AudioType.RIGHT_CLICK);
            if (unitSelected != null) {
                // Check distance
                if (unitSelected instanceof ArcherUnit) {
                    // Convert closest unit to click
                    double[] screenPos = camera.getDrawingPosition(
                            closestUnit.getAverageX(),
                            closestUnit.getAverageY());
                    // If it's an archer unit, check the faction and distance from the closest unit from view.camera
                    if (closestUnit.getPoliticalFaction() != unitSelected.getPoliticalFaction() &&
                            MathUtils.squareDistance(mouseX, mouseY, screenPos[0], screenPos[1]) <
                                    CameraConstants.SQUARE_CLICK_ATTACK_DISTANCE) {
                        ((ArcherUnit) unitSelected).setUnitFiredAt(closestUnit);
                        if (unitSelected.getState() == UnitState.MOVING) {
                            unitSelected.moveFormationKeptTo(unitSelected.getAnchorX(), unitSelected.getAnchorY(), unitSelected.getAnchorAngle());
                        }
                    } else {
                        unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                        ((ArcherUnit) unitSelected).setUnitFiredAt(null);
                    }
                } else if (unitSelected instanceof BallistaUnit) {
                    // Convert closest unit to click
                    double[] screenPos = camera.getDrawingPosition(
                            closestUnit.getAverageX(),
                            closestUnit.getAverageY());
                    // If it's an archer unit, check the faction and distance from the closest unit from view.camera
                    if (closestUnit.getPoliticalFaction() != unitSelected.getPoliticalFaction() &&
                            MathUtils.squareDistance(mouseX, mouseY, screenPos[0], screenPos[1]) <
                                    CameraConstants.SQUARE_CLICK_ATTACK_DISTANCE) {
                        ((BallistaUnit) unitSelected).setUnitFiredAt(closestUnit);
                        if (unitSelected.getState() == UnitState.MOVING) {
                            unitSelected.moveFormationKeptTo(unitSelected.getAnchorX(), unitSelected.getAnchorY(), unitSelected.getAnchorAngle());
                        }
                    } else {
                        unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                        ((BallistaUnit) unitSelected).setUnitFiredAt(null);
                    }
                } else if (unitSelected instanceof CatapultUnit) {
                    // Convert closest unit to click
                    double[] screenPos = camera.getDrawingPosition(
                            closestUnit.getAverageX(),
                            closestUnit.getAverageY());
                    // If it's an archer unit, check the faction and distance from the closest unit from view.camera
                    if (closestUnit.getPoliticalFaction() != unitSelected.getPoliticalFaction() &&
                            MathUtils.squareDistance(mouseX, mouseY, screenPos[0], screenPos[1]) <
                                    CameraConstants.SQUARE_CLICK_ATTACK_DISTANCE) {
                        ((CatapultUnit) unitSelected).setUnitFiredAt(closestUnit);
                        if (unitSelected.getState() == UnitState.MOVING) {
                            unitSelected.moveFormationKeptTo(unitSelected.getAnchorX(), unitSelected.getAnchorY(), unitSelected.getAnchorAngle());
                        }
                    } else {
                        unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                        ((CatapultUnit) unitSelected).setUnitFiredAt(null);
                    }
                } else {
                    unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                }
                double[] actualCurrent = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                double distance = MathUtils.quickDistance(
                        rightClickActualX, rightClickActualY, actualCurrent[0], actualCurrent[1]);
                if (distance > ControlConstants.MINIMUM_WIDTH_SELECTION * unitSelected.getUnitStats().spacing) {
                    int frontlineWidth = Math.min((int) (
                                    distance / unitSelected.getUnitStats().spacing),
                            unitSelected.getNumAlives());
                    unitSelected.changeFrontlineWidth(frontlineWidth);
                }
            }
        }
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        // When scrolling, immediately cancelled the current right clicked.
        if (rightClickedNotReleased) {
            rightClickedNotReleased = false;
        }
        float scrollVal = event.getCount();
        if (!drawingSettings.isSmoothCameraMovement()) {
            // Non-smooth zoom processing
            if (scrollVal < 0) {
                // Zoom in
                camera.setZoom(camera.getZoom() * CameraConstants.ZOOM_PER_SCROLL);
            } else if (scrollVal > 0) {
                // Zoom out
                camera.setZoom(camera.getZoom() / CameraConstants.ZOOM_PER_SCROLL);
                if (camera.getZoom() < CameraConstants.MINIMUM_ZOOM) camera.setZoom(CameraConstants.MINIMUM_ZOOM);
            }
        } else {
            // Smooth-zoom processing
            if (scrollVal < 0) {
                // Zoom in
                zoomGoal *= CameraConstants.ZOOM_PER_SCROLL;
                if (zoomGoal > CameraConstants.MAXIMUM_ZOOM) zoomGoal = CameraConstants.MAXIMUM_ZOOM;
            } else if (scrollVal > 0) {
                // Zoom out
                zoomGoal /= CameraConstants.ZOOM_PER_SCROLL;
                if (zoomGoal < CameraConstants.MINIMUM_ZOOM) zoomGoal = CameraConstants.MINIMUM_ZOOM;
            }
            zoomCounter = drawingSettings.getSmoothCameraSteps();
        }
    }

    public static void main(String... args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        PApplet.main("MainSimulation");
    }

    @Override
    public void keyPressed() {
        if (key == 'c') {
            drawingSettings.setDrawTroopInDanger(!drawingSettings.isDrawTroopInDanger());
        }
    }

    /**
     *  __  __           _      _    _       _ _      ____        _   _           _          _   _
     * |  \/  |         | |    | |  | |     (_) |    / __ \      | | (_)         (_)        | | (_)
     * | \  / | __ _ ___| | __ | |  | |_ __  _| |_  | |  | |_ __ | |_ _ _ __ ___  _ ______ _| |_ _  ___  _ __
     * | |\/| |/ _` / __| |/ / | |  | | '_ \| | __| | |  | | '_ \| __| | '_ ` _ \| |_  / _` | __| |/ _ \| '_ \
     * | |  | | (_| \__ \   <  | |__| | | | | | |_  | |__| | |_) | |_| | | | | | | |/ / (_| | |_| | (_) | | | |
     * |_|  |_|\__,_|___/_|\_\  \____/|_| |_|_|\__|  \____/| .__/ \__|_|_| |_| |_|_/___\__,_|\__|_|\___/|_| |_|
     *                                                     | |
     *                                                     |_|
     */

    /**
     * Create a mask based on unit that is roughly in position
     */
    private int[] unitMask(PImage image, IntMatrix2D alpha, BaseUnit unit) {
        boolean[][] inPositionMask = unit.getTroopsInPosition();
        double singleSpacing = unit.getSpacing() * CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        IntMatrix2D imageMap = new DenseIntMatrix2D(image.height, image.width);
        for (int i = 0; i < unit.getDepth(); i++) {
            for (int j = 0; j < unit.getWidth(); j++) {
                if (inPositionMask[i][j]) {
                    IntMatrix2D imagePart = imageMap.viewPart((int) (i * singleSpacing), (int) (j * singleSpacing),
                            (int) (singleSpacing), (int) (singleSpacing));
                    IntMatrix2D alphaPart = alpha.viewPart((int) (i * singleSpacing), (int) (j * singleSpacing),
                            (int) (singleSpacing), (int) (singleSpacing));
                    imagePart.assign(alphaPart, new IntIntFunction() {
                        @Override
                        public int apply(int i, int i1) {
                            return Math.min(i, i1);
                        }
                    });
                }
            }
        }
        return ((DenseIntMatrix2D) imageMap).elements();
    }

    /**
     * Draw all troop that is in position in a masked image.
     */
    private void drawMaskedInPositionUnit(BaseUnit unit, Camera camera, DrawingSettings settings) {
        if (settings.isDrawTroopShadow()) {
            Pair<PImage, IntMatrix2D> imageAndAlpha = unitShadowSimplifiedImageMap.get(unit);
            PImage img = imageAndAlpha.getKey();
            IntMatrix2D alpha = imageAndAlpha.getValue();
            int[] mask = unitMask(img, alpha, unit);
            img.mask(mask);

            double[] drawingPosition = camera.getDrawingPosition(unit.getAnchorX(), unit.getAnchorY());
            double drawX = drawingPosition[0];
            double drawY = drawingPosition[1];

            double relativeUnitImageScale = camera.getZoom() / CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
            pushMatrix();
            translate((float) (drawX + shadowXOffset), (float) (drawY + shadowYOffset));
            rotate((float) (unit.getAnchorAngle() - camera.getAngle() + Math.PI / 2));
            image(img, (float) (- img.width / 2 * relativeUnitImageScale),
                    (float) (- unit.getSpacing() * CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE / 2 * relativeUnitImageScale),
                    (float) (img.width * relativeUnitImageScale),
                    (float) (img.height * relativeUnitImageScale));
            popMatrix();
        }

        Pair<PImage, IntMatrix2D> imageAndAlpha = unitSimplifiedImageMap.get(unit);
        PImage img = imageAndAlpha.getKey();
        IntMatrix2D alpha = imageAndAlpha.getValue();
        int[] mask = unitMask(img, alpha, unit);
        img.mask(mask);

        double[] drawingPosition = camera.getDrawingPosition(unit.getAnchorX(), unit.getAnchorY());
        double drawX = drawingPosition[0];
        double drawY = drawingPosition[1];

        double relativeUnitImageScale = camera.getZoom() / CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        pushMatrix();
        translate((float) drawX, (float) drawY);
        rotate((float) (unit.getAnchorAngle() - camera.getAngle() + Math.PI / 2));
        image(img, (float) (- img.width / 2 * relativeUnitImageScale),
                (float) (- unit.getSpacing() * CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE / 2 * relativeUnitImageScale),
                (float) (img.width * relativeUnitImageScale),
                (float) (img.height * relativeUnitImageScale));
        popMatrix();
    }

    /**
     * Create a simplified image and shadow image of the unit. This helps with drawer optimization since troop that is
     * roughly in their supposed position can just be drawn using an image view.map instead of being drawn individually
     */
    private PImage createSimplifiedUnitImage(BaseUnit unit) {
        double singleSpacing = unit.getSpacing() * CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        double singleSize = unit.getTroops().get(0).getSize() * CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        double imageWidth = unit.getWidth() * singleSpacing;
        double imageHeight = unit.getDepth() * singleSpacing;

        // Now, start to create and draw the image
        double topLeftX = singleSpacing / 2;
        double topLeftY = singleSpacing / 2;
        double x;
        double y;
        PGraphics pg = createGraphics((int) imageWidth, (int) imageHeight, P3D);
        pg.beginDraw();
        pg.noStroke();
        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        pg.background(color[0], color[1], color[2], 0);
        for (int i = 0; i < unit.getWidth(); i++) {
            for (int j = 0; j < unit.getDepth(); j++) {
                x = topLeftX + singleSpacing * i;
                y = topLeftY + singleSpacing * j;
                pg.fill(color[0], color[1], color[2], color[3]);
                pg.ellipse((float) x, (float) y, (float) singleSize, (float) singleSize);
            }
        }
        pg.endDraw();
        return pg.get();
    }

    private PImage createSimplifiedShadowUnitImage(BaseUnit unit) {
        double singleSpacing = unit.getSpacing() * CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE;
        double shadowSize = unit.getTroops().get(0).getSize() * CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE
                * UniversalConstants.SHADOW_SIZE;
        double imageWidth = unit.getWidth() * singleSpacing;
        double imageHeight = unit.getDepth() * singleSpacing;

        // Now, start to create and draw the image
        double topLeftX = singleSpacing / 2;
        double topLeftY = singleSpacing / 2;
        double x;
        double y;
        PGraphics pg = createGraphics((int) imageWidth, (int) imageHeight, P3D);
        pg.beginDraw();
        pg.noStroke();
        int[] color = UniversalConstants.SHADOW_COLOR;
        pg.background(color[0], color[1], color[2], 0);
        for (int i = 0; i < unit.getWidth(); i++) {
            for (int j = 0; j < unit.getDepth(); j++) {
                x = topLeftX + singleSpacing * i;
                y = topLeftY + singleSpacing * j;
                pg.fill(color[0], color[1], color[2], color[3]);
                pg.ellipse((float) x, (float) y, (float) shadowSize, (float) shadowSize);
            }
        }
        pg.endDraw();
        return pg.get();
    }

    /**
     *   _____                                                          _               _          _
     *  / ____|                                                        (_)             | |        | |
     * | |  __  __ _ _ __ ___   ___   _ __  _ __ ___   ___ ___  ___ ___ _ _ __   __ _  | |__   ___| |_ __   ___ _ __
     * | | |_ |/ _` | '_ ` _ \ / _ \ | '_ \| '__/ _ \ / __/ _ \/ __/ __| | '_ \ / _` | | '_ \ / _ \ | '_ \ / _ \ '__|
     * | |__| | (_| | | | | | |  __/ | |_) | | | (_) | (_|  __/\__ \__ \ | | | | (_| | | | | |  __/ | |_) |  __/ |
     *  \_____|\__,_|_| |_| |_|\___| | .__/|_|  \___/ \___\___||___/___/_|_| |_|\__, | |_| |_|\___|_| .__/ \___|_|
     *                               | |                                         __/ |              | |
     *                               |_|                                        |___/               |_|
     */

    void preprocessSimplifiedUnitImages() {
        // Create an image version of troops. This helps with drawer optimization when ther are a lot of troops in
        // the field.
        for (BaseUnit unit : env.getUnits()) {
            PImage img = createSimplifiedUnitImage(unit);
            int[][] alpha = DrawingUtils.getAlphaArray(img);
            IntMatrix2D alphaArray = new DenseIntMatrix2D(alpha);
            unitSimplifiedImageMap.put(unit, new Pair<>(img, alphaArray));

            PImage shadow = createSimplifiedShadowUnitImage(unit);
            int[][] shadowAlpha = DrawingUtils.getAlphaArray(shadow);
            IntMatrix2D shadowAlphaArray = new DenseIntMatrix2D(shadowAlpha);
            unitShadowSimplifiedImageMap.put(unit, new Pair<>(shadow, shadowAlphaArray));
        }
    }

    /**
     * Load all tile from view.map, given the top left position.
     * Path must contains tile images
     * Each image file name must be in the format IMG-i-j.png, in which i and j is the row and column position of each
     * tile when all tile is laid out into the view.map.
     * @param x top left x position.
     * @param y top left y position.
     * @param path path to the folder containing images of all tiles.
     * @param tileSize the size of each tile.
     */
    private void loadMapTiles(double x, double y, String path, double tileSize) {

        // Initialize the tile list
        tiles = new ArrayList<>();

        // Load all the tiles
        Pattern numberPattern = Pattern.compile("[0-9]+");
        File folder = new File(Paths.get(path).toString());
        File[] fileList = folder.listFiles();

        for (File file : fileList) {
            String fileName = file.getName();
            Matcher m = numberPattern.matcher(fileName);
            m.find();
            int row = Integer.valueOf(m.group());
            m.find();
            int col = Integer.valueOf(m.group());
            PImage img = loadImage(Paths.get(path, fileName).toString());
            tiles.add(new Tile(x + col * tileSize, y + row * tileSize, tileSize, img));
        }
    }

    /**
     * Update the size view.map baseded on view.camera zoom
     */
    private void updateSizeMaps() {

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
     *   ____  _     _           _     _____
     *  / __ \| |   (_)         | |   |  __ \
     * | |  | | |__  _  ___  ___| |_  | |  | |_ __ __ ___      _____ _ __
     * | |  | | '_ \| |/ _ \/ __| __| | |  | | '__/ _` \ \ /\ / / _ \ '__|
     * | |__| | |_) | |  __/ (__| |_  | |__| | | | (_| |\ V  V /  __/ |
     *  \____/|_.__/| |\___|\___|\__| |_____/|_|  \__,_| \_/\_/ \___|_|
     *             _/ |
     *            |__/
     */

    void drawTerrain(Terrain terrain, Camera camera) {

        rectMode(CORNER);
        double[][] cameraBoundingBox = {
                {0, 0},
                {camera.getWidth(), 0},
                {camera.getWidth(), camera.getHeight()},
                {0, camera.getHeight()}
        };
        double topX = terrain.getTopX();
        double topY = terrain.getTopY();
        double div = terrain.getDiv();
        for (int i = 0; i < terrain.getNumX(); i++) {
            for (int j = 0; j < terrain.getNumY(); j++) {
                double[] drawingPos1 = camera.getDrawingPosition(topX + i * div, topY + j * div);
                double[] drawingPos2 = camera.getDrawingPosition(topX + (i + 1) * div, topY + j * div);
                double[] drawingPos3 = camera.getDrawingPosition(topX + i * div, topY + (j + 1) * div);
                double[] drawingPos4 = camera.getDrawingPosition(topX + (i + 1) * div, topY + (j + 1) * div);
                if (DrawingUtils.drawable(drawingPos1[0], drawingPos1[1], INPUT_WIDTH, INPUT_HEIGHT) ||
                    DrawingUtils.drawable(drawingPos2[0], drawingPos2[1], INPUT_WIDTH, INPUT_HEIGHT) ||
                    DrawingUtils.drawable(drawingPos3[0], drawingPos3[1], INPUT_WIDTH, INPUT_HEIGHT) ||
                    DrawingUtils.drawable(drawingPos4[0], drawingPos4[1], INPUT_WIDTH, INPUT_HEIGHT)) {

                    pushMatrix();
                    translate((float) drawingPos1[0], (float) drawingPos1[1]);
                    rotate((float) -camera.getAngle());
                    fill(DrawingConstants.COLOR_TERRAIN_DOT[0],
                            DrawingConstants.COLOR_TERRAIN_DOT[1],
                            DrawingConstants.COLOR_TERRAIN_DOT[2],
                            (float) (DrawingConstants.COLOR_TERRAIN_DOT[3] * terrain.getHeightFromTileIndex(i, j) / (terrain.getMaxZ() - terrain.getMinZ())));
                    rect(0, 0, (float) (terrain.getDiv() * camera.getZoom()),
                            (float) (terrain.getDiv() * camera.getZoom()));
                    popMatrix();
                }
            }
        }
        rectMode(CENTER);
    }

    void drawTerrainLine(Terrain terrain, Camera camera) {
        int[] gridLimits = DrawingUtils.getVisibleGridBoundary(terrain, camera);
        int minX = gridLimits[0];
        int maxX = gridLimits[1];
        int minY = gridLimits[2];
        int maxY = gridLimits[3];
        noFill();
        strokeWeight(1);
        int[] color = DrawingConstants.COLOR_TERRAIN_LINE;
        stroke(color[0], color[1], color[2], DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA);
        for (int i = minX; i < maxX; i++) {
            beginShape();
            // Begin line
            double[] beginPos = terrain.getPosFromTileIndex(i, minY);
            double beginZ = terrain.getHeightFromTileIndex(i, minY);
            double[] drawBegin = camera.getDrawingPosition(beginPos[0], beginPos[1], beginZ);
            vertex((float) drawBegin[0], (float) drawBegin[1]);

            for (int j = minY; j < maxY; j++) {

                // NextPoint
                double[] nextPos = terrain.getPosFromTileIndex(i, j + 1);
                double nextZ = terrain.getHeightFromTileIndex(i, j + 1);
                double[] drawNext = camera.getDrawingPosition(nextPos[0], nextPos[1], nextZ);

                // Draw line
                stroke(color[0], color[1], color[2],
                        (float) ((nextZ - terrain.getMinZ()) / (terrain.getMaxZ() - terrain.getMinZ()) *
                                DrawingConstants.COLOR_TERRAIN_LINE_ALPHA_RANGE +
                                DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA));
                vertex((float) drawNext[0], (float) drawNext[1]);
            }
            endShape();
        }

        for (int j = minY; j < maxY; j++) {
            // Begin line
            beginShape();
            double[] beginPos = terrain.getPosFromTileIndex(minX, j);
            double beginZ = terrain.getHeightFromTileIndex(minX, j);
            double[] drawBegin = camera.getDrawingPosition(beginPos[0], beginPos[1], beginZ);
            vertex((float) drawBegin[0], (float) drawBegin[1]);

            for (int i = minX; i < maxX; i++) {

                // End line
                double[] nextPos = terrain.getPosFromTileIndex(i + 1, j);
                double nextZ = terrain.getHeightFromTileIndex(i + 1, j);
                double[] drawNext = camera.getDrawingPosition(nextPos[0], nextPos[1], nextZ);

                // Draw line
                stroke(color[0], color[1], color[2],
                        (float) ((nextZ - terrain.getMinZ()) / (terrain.getMaxZ() - terrain.getMinZ()) *
                                DrawingConstants.COLOR_TERRAIN_LINE_ALPHA_RANGE +
                                DrawingConstants.COLOR_TERRAIN_LINE_MIN_ALPHA));
                vertex((float) drawNext[0], (float) drawNext[1]);
            }
            endShape();
        }
        strokeWeight(0);
    }

    void drawObject(BaseObject object, Camera camera, Terrain terrain, DrawingSettings settings) {

        // Recalculate position and shape based on the view.camera position
        double z = object.getHeight()
                + terrain.getHeightFromPos(object.getX(), object.getY()) + object.getHeight();
        double[] position = camera.getDrawingPosition(
                object.getX(),
                object.getY(),
                z);
        double drawX = position[0];
        double drawY = position[1];

        // Check if the object is drawable.
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // Draw the object
        double angle = camera.getDrawingAngle(object.getAngle());
        if (object instanceof Arrow) {
            fill(50, 50, 50);
            shapeDrawer.arrow(g,
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.ARROW_SIZE, settings);
        }
        if (object instanceof Ballista) {
            fill(50, 50, 50);
            shapeDrawer.arrow(g,
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.BALISTA_SIZE, settings);
        }
        if (object instanceof Stone) {
            fill(50, 50, 50);
            shapeDrawer.circleShape(g,
                    (float) drawX, (float) drawY,
                    (float) (camera.getZoomAtHeight(z) * DrawingConstants.CATAPULT_SIZE), camera);
        }
    }

    void drawObjectCarriedByTroop(int lifeTime, BaseObject object, BaseSingle single, Terrain terrain, DrawingSettings settings) {

        // Recalculate object actual position
        Pair<Double, Double> rotatedVector = MathUtils.rotate(object.getX(), object.getY(), single.getAngle());
        double z = terrain.getHeightFromPos(single.getX(), single.getY());

        // Recalculate position and shape based on the view.camera position
        double[] position = camera.getDrawingPosition(
                rotatedVector.getKey() +
                        single.getX(),
                rotatedVector.getValue() +
                        single.getY(),
                z);
        double drawX = position[0];
        double drawY = position[1];

        // Draw the object
        int opacity = (int) (255 * Math.min(1.0, 1.0 * lifeTime / UniversalConstants.CARRIED_OBJECT_FADEAWAY));
        double angle = camera.getDrawingAngle(object.getAngle() + single.getAngle());
        if (object instanceof Arrow) {
            fill(50, 50, 50, opacity);
            shapeDrawer.arrow(g,
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.ARROW_SIZE, settings);
        } else if (object instanceof Ballista) {
            fill(50, 50, 50, opacity);
            shapeDrawer.arrow(g,
                    (float) drawX, (float) drawY, (float) angle,
                    (float) (camera.getZoomAtHeight(z)), (float) DrawingConstants.BALISTA_SIZE, settings);
        }
    }

    /**
     *  _    _       _ _         _____
     * | |  | |     (_) |       |  __ \
     * | |  | |_ __  _| |_ ___  | |  | |_ __ __ ___      _____ _ __
     * | |  | | '_ \| | __/ __| | |  | | '__/ _` \ \ /\ / / _ \ '__|
     * | |__| | | | | | |_\__ \ | |__| | | | (_| |\ V  V /  __/ |
     *  \____/|_| |_|_|\__|___/ |_____/|_|  \__,_| \_/\_/ \___|_|
     */

    /**
     * Draw unit arrow plan. This arrow indicates the potential position that the unit is moving to.
     */
    void drawArrowPlanAtHeight(double beginX, double beginY, double endX, double endY, double height, Camera camera,
                               DrawingSettings settings) {
        double angle = MathUtils.atan2(endY - beginY, endX - beginX);
        double rightAngle = angle + Math.PI / 2;

        double upUnitX = MathUtils.quickCos((float) angle) * DrawingConstants.ARROW_SIZE;
        double upUnitY = MathUtils.quickSin((float) angle) * DrawingConstants.ARROW_SIZE;
        double rightUnitX = MathUtils.quickCos((float) rightAngle) * DrawingConstants.ARROW_SIZE;
        double rightUnitY = MathUtils.quickSin((float) rightAngle) * DrawingConstants.ARROW_SIZE;

        double[][] arrow = {
                {beginX + rightUnitX * 0.8, beginY + rightUnitY * 0.8},
                {endX + rightUnitX * 0.66 - upUnitX * 2.4, endY + rightUnitY * 0.66 - upUnitY * 2.4},
                {endX + rightUnitX * 1.66 - upUnitX * 3, endY + rightUnitY * 1.66 - upUnitY * 3},
                {endX, endY},
                {endX - rightUnitX * 1.66 - upUnitX * 3, endY - rightUnitY * 1.66 - upUnitY * 3},
                {endX - rightUnitX * 0.66 - upUnitX * 2.4, endY - rightUnitY * 0.66 - upUnitY * 2.4},
                {beginX - rightUnitX * 0.8, beginY - rightUnitY * 0.8},
        };

        beginShape();
        for (int i = 0; i < arrow.length; i++) {
            double[] drawingPosition = camera.getDrawingPosition(arrow[i][0], arrow[i][1], height);
            vertex((float) drawingPosition[0], (float) drawingPosition[1]);
        }
        endShape(CLOSE);
    }

    /**
     * Draw unit arrow plan. This arrow indicates the potential position that the unit is moving to.
     */
    void drawArrowPlan(double beginX, double beginY, double endX, double endY, Camera camera, DrawingSettings settings) {
        double angle = MathUtils.atan2(endY - beginY, endX - beginX);
        double rightAngle = angle + Math.PI / 2;

        double upUnitX = MathUtils.quickCos((float) angle) * DrawingConstants.ARROW_SIZE;
        double upUnitY = MathUtils.quickSin((float) angle) * DrawingConstants.ARROW_SIZE;
        double rightUnitX = MathUtils.quickCos((float) rightAngle) * DrawingConstants.ARROW_SIZE;
        double rightUnitY = MathUtils.quickSin((float) rightAngle) * DrawingConstants.ARROW_SIZE;

        double[][] arrow = {
                {beginX + rightUnitX * 0.8, beginY + rightUnitY * 0.8},
                {endX + rightUnitX * 0.66 - upUnitX * 2.4, endY + rightUnitY * 0.66 - upUnitY * 2.4},
                {endX + rightUnitX * 1.66 - upUnitX * 3, endY + rightUnitY * 1.66 - upUnitY * 3},
                {endX, endY},
                {endX - rightUnitX * 1.66 - upUnitX * 3, endY - rightUnitY * 1.66 - upUnitY * 3},
                {endX - rightUnitX * 0.66 - upUnitX * 2.4, endY - rightUnitY * 0.66 - upUnitY * 2.4},
                {beginX - rightUnitX * 0.8, beginY - rightUnitY * 0.8},
        };

        beginShape();
        for (int i = 0; i < arrow.length; i++) {
            double[] drawingPosition = camera.getDrawingPosition(arrow[i][0], arrow[i][1]);
            vertex((float) drawingPosition[0], (float) drawingPosition[1]);
        }
        endShape(CLOSE);
    }

    /**
     * Draw the block representation of the goal
     */
    void drawGoalUnitBlock(BaseUnit unit, Camera camera, DrawingSettings settings, boolean hovered) {
        // draw the bounding box.
        double[][] boundingBox = unit.getGoalBoundingBox();

        // Convert to drawer points
        double[] p1 = camera.getDrawingPosition(boundingBox[0][0], boundingBox[0][1]);
        double[] p2 = camera.getDrawingPosition(boundingBox[1][0], boundingBox[1][1]);
        double[] p3 = camera.getDrawingPosition(boundingBox[2][0], boundingBox[2][1]);
        double[] p4 = camera.getDrawingPosition(boundingBox[3][0], boundingBox[3][1]);

        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        fill(color[0], color[1], color[2], 150);
        beginShape();
        vertex((float) p1[0], (float) p1[1]);
        vertex((float) p2[0], (float) p2[1]);
        vertex((float) p3[0], (float) p3[1]);
        vertex((float) p4[0], (float) p4[1]);
        endShape(CLOSE);
    }

    /**
     * Draw the block representing the entire unit.
     */
    void drawUnitBlock(BaseUnit unit, Camera camera, DrawingSettings settings, boolean hovered) {

        // draw the bounding box.
        double[][] boundingBox = unit.getBoundingBox();

        // Convert to drawer points
        double[] p1 = camera.getDrawingPosition(boundingBox[0][0], boundingBox[0][1]);
        double[] p2 = camera.getDrawingPosition(boundingBox[1][0], boundingBox[1][1]);
        double[] p3 = camera.getDrawingPosition(boundingBox[2][0], boundingBox[2][1]);
        double[] p4 = camera.getDrawingPosition(boundingBox[3][0], boundingBox[3][1]);
        double[] p5 = camera.getDrawingPosition(boundingBox[4][0], boundingBox[4][1]);
        double[] p6 = camera.getDrawingPosition(boundingBox[5][0], boundingBox[5][1]);

        // First, draw the box indicating the unit at full strength
        fill(DrawingConstants.UNIT_SIZE_COLOR[0],
                DrawingConstants.UNIT_SIZE_COLOR[1],
                DrawingConstants.UNIT_SIZE_COLOR[2],
                DrawingConstants.UNIT_SIZE_COLOR[3]);
        beginShape();
        vertex((float) p1[0], (float) p1[1]);
        vertex((float) p2[0], (float) p2[1]);
        vertex((float) p5[0], (float) p5[1]);
        vertex((float) p6[0], (float) p6[1]);
        endShape(CLOSE);

        // Then draw the box indicating the actual current scale of the the unit
        int[] shadowColor = UniversalConstants.SHADOW_COLOR;
        fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
        beginShape();
        vertex((float) (p1[0] + unitShadowXOffset * 2), (float) (p1[1] + unitShadowYOffset * 2));
        vertex((float) (p2[0] + unitShadowXOffset * 2), (float) (p2[1] + unitShadowYOffset * 2));
        vertex((float) (p3[0] + unitShadowXOffset * 2), (float) (p3[1] + unitShadowYOffset * 2));
        vertex((float) (p4[0] + unitShadowXOffset * 2), (float) (p4[1] + unitShadowYOffset * 2));
        endShape(CLOSE);

        int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
        if (closestUnit == unit) {
            fill(color[0], color[1], color[2], 255);
        } else {
            fill(color[0], color[1], color[2], 255);
        }
        if (unitSelected == unit) {
            fill(color[0], color[1], color[2], 255);
        }
        beginShape();
        vertex((float) p1[0], (float) p1[1]);
        vertex((float) p2[0], (float) p2[1]);
        vertex((float) p3[0], (float) p3[1]);
        vertex((float) p4[0], (float) p4[1]);
        endShape(CLOSE);
    }

    /**
     * Portray alive troop. The troop is only portrayable if
     */
    void portrayAliveSingle(BaseSingle single, Camera camera, DrawingSettings settings, Terrain terrain, boolean hovered) {
        // Recalculate position and shape based on the view.camera position
        double singleX = single.getX();
        double singleY = single.getY();
        double singleZ = terrain.getHeightFromPos(single.getX(), single.getY());
        double[] position = camera.getDrawingPosition(singleX, singleY, singleZ);
        double drawX = position[0];
        double drawY = position[1];
        double zoomAdjustment = camera.getZoomAtHeight(singleZ) / camera.getZoom();

        // Check if the object is drawable. If not, don't portray it.
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // If it's drawable, draw the alive unit and potentially add some sound
        soundAliveSingle(single);
        if (drawingSettings.isInPositionOptimization() &&
                camera.getZoomAtHeight(singleZ) < CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE &&
                single.isInPosition() && !single.isInDanger()) {
            // Don't draw if troop is in position and zoom is small. This will be handled by a mask image to improve
            // optimization.
            return;
        }

        // Draw all the object sticking to the individual
        HashMap<BaseObject, Integer> carriedObjects = single.getCarriedObjects();
        for (BaseObject obj : carriedObjects.keySet()) {
            drawObjectCarriedByTroop(carriedObjects.get(obj), obj, single, terrain, settings);
        }
        drawAliveSingle(drawX, drawY, zoomAdjustment, single, camera, settings, hovered);
    }

    /**
     * Add sound created by the alive troop. These will include attack, defend and pain sounds.
     */
    void soundAliveSingle(BaseSingle single) {

        // If sound effect not turned on don't play anything
        if (!audioSettings.isSoundEffect()) return;

        // Pain sound if the unit just got hit
        if (MathUtils.randUniform() < AudioConstants.SCREAM_TENDENCY) {
            if (single.getJustHit() > 0) {
                // Perform pain sound here.
            }
        }
    }

    /**
     * Draw alive unit
     */
    void drawAliveSingle(double drawX, double drawY, double zoomAdjustment, BaseSingle single, Camera camera, DrawingSettings settings, boolean hovered) {

        // Check if the object is drawable. If not, don't portray it.
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // Pre calculate drawer information
        double angle = camera.getDrawingAngle(single.getFacingAngle());
        double unitX = MathUtils.quickCos((float) angle);
        double unitY = MathUtils.quickSin((float) angle);

        // Fill the color by political faction
        int[] color = DrawingUtils.getFactionColor(single.getPoliticalFaction());
        int[] modifiedColor = new int[4];
        int[] shadowColor = UniversalConstants.SHADOW_COLOR;

        // Modify the color by the amount of damage sustain
        if (settings.isDrawDamageSustained()) {
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
        if (env.getGameSettings().isBorderInwardCollision() && drawingSettings.isDrawTroopInDanger() &&
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
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.cavalryShape(g,
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                         angle,
                        (currSizeCavalry[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        camera);
            }

            // Calvary shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.cavalryShape(g,
                    drawX, drawY,
                    angle, (currSizeCavalry[0] * zoomAdjustment),
                    camera);

            // Eye
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                fill(0, 0, 0, 128);
                ellipse((float) (drawX + (unitX * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.25)),
                        (float) (drawY + (unitY * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.25)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Cavalry Sword
            if (settings.getDrawWeapon() == DrawingMode.DRAW) {
                fill(50, 50, 50);
                double diaRightUnitX = MathUtils.quickCos((float) (angle + Math.PI / 2));
                double diaRightUnitY = MathUtils.quickSin((float) (angle + Math.PI / 2));
                shapeDrawer.sword(g,
                        (drawX + (diaRightUnitX * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        (drawY + (diaRightUnitY * currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        angle,
                        (camera.getZoom() * zoomAdjustment),
                        settings);
            }
        } else if (single instanceof ArcherSingle) {
            // Archer shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset * zoomAdjustment), (float) (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeArcher[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeArcher[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment), camera);
            }

            // Archer circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY,
                    (currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeArcher[INDEX_TROOP_SIMPLIED_SIZE]  * zoomAdjustment), camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (drawY + (unitY * currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Archer bow
            fill(50, 50, 50);
            shapeDrawer.bow(g, (float) drawX, (float) drawY, (float) angle,
                    (float) (currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment), settings);

        } else if (single instanceof BallistaSingle) {
            // Balista shadow
            // TODO: Change the shape to the balista shape
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g,
                        (float) (drawX + shadowXOffset * zoomAdjustment),
                        (float) (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeBalista[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeBalista[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment), camera);
            }

            // Balista circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g, (float) drawX, (float) drawY,
                    (currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeBalista[INDEX_TROOP_SIMPLIED_SIZE]  * zoomAdjustment), camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (drawY + (unitY * currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment * 0.3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Archer bow
            fill(50, 50, 50);
            shapeDrawer.bow(g, (float) drawX, (float) drawY, (float) angle,
                    (float) (currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment), settings);

        } else if (single instanceof SkirmisherSingle) {
            // Skirmisher shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g,
                        (float) (drawX + shadowXOffset * zoomAdjustment),
                        (float) (drawY + shadowYOffset * zoomAdjustment),
                        (float) (currSizeSkirmisher[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (float) (currSizeSkirmisher[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment), camera);
            }

            // Skirmisher circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g,
                    (float) drawX,
                    (float) drawY,
                    (float) (currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (float) (currSizeSkirmisher[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment),
                    camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment / 2 * .6)),
                        (float) (drawY + (unitY * currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment / 2 * .6)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }
        } else if (single instanceof SlingerSingle) {
            // Slinger shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g, (float) (drawX + shadowXOffset * zoomAdjustment), (float) (drawY + shadowYOffset * zoomAdjustment),
                        (float) (currSizeSlinger[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (float) (currSizeSlinger[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment), camera);
            }

            // Slinger circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g,
                    (float) drawX,
                    (float) drawY,
                    (float) (currSizeSlinger[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (float) (currSizeSlinger[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment), camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSlinger[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (drawY + (unitY * currSizeSlinger[INDEX_TROOP_SIZE] * 0.3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }
        } else if (single instanceof PhalanxSingle) {
            // Phalanx shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g,
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                        (currSizePhalanx[INDEX_SHADOW_SIZE]  * zoomAdjustment),
                        (currSizePhalanx[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment), camera);
            }

            // Phalanx circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    (currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizePhalanx[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment),
                    camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * .3)),
                        (float) (drawY + (unitY * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * .3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Spear
            if (settings.getDrawWeapon() == DrawingMode.DRAW) {
                if (camera.getZoom() > CameraConstants.ZOOM_RENDER_LEVEL_PERCEPTIVE || single.getUnit().getTroopIndex(single) / width < 5) {
                    fill(50, 50, 50);
                    double diaRightUnitX = MathUtils.quickCos((float)(angle + Math.PI / 2));
                    double diaRightUnitY = MathUtils.quickSin((float)(angle + Math.PI / 2));
                    shapeDrawer.spear(g,
                            (float) (drawX + diaRightUnitX * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45),
                            (float) (drawY + diaRightUnitY * currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45),
                            (float) angle, 100,
                            (float) (camera.getZoom() * zoomAdjustment), settings);
                }
            }

        } else if (single instanceof SwordmanSingle) {
            // Swordman shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g,
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment), camera);
            }

            // Swordman circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    (currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment), camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSwordman[0] * zoomAdjustment * .3)),
                        (float) (drawY + (unitY * currSizeSwordman[0] * zoomAdjustment * .3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }

            // Sword
            if (settings.getDrawWeapon() == DrawingMode.DRAW) {
                fill(50, 50, 50);
                double diaRightUnitX = MathUtils.quickCos((float)(angle + Math.PI / 2));
                double diaRightUnitY = MathUtils.quickSin((float)(angle + Math.PI / 2));
                shapeDrawer.sword(g,
                        (drawX + (diaRightUnitX * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        (drawY + (diaRightUnitY * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment * 0.45)),
                        angle,
                        (camera.getZoom() * zoomAdjustment),
                        settings);
            }
        } else {
            // Swordman shadow
            if (drawingSettings.isDrawTroopShadow()) {
                fill(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]);
                shapeDrawer.infantryShape(g,
                        (drawX + shadowXOffset * zoomAdjustment),
                        (drawY + shadowYOffset * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIZE] * zoomAdjustment),
                        (currSizeSwordman[INDEX_SHADOW_SIMPLIED_SIZE] * zoomAdjustment), camera);
            }

            // Swordman circle shape
            fill(modifiedColor[0], modifiedColor[1], modifiedColor[2], modifiedColor[3]);
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    (currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment),
                    (currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment), camera);

            // Eye
            fill(0, 0, 0, 128);
            if (settings.getDrawEye() == DrawingMode.DRAW) {
                ellipse((float) (drawX + (unitX * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment / .3)),
                        (float) (drawY + (unitY * currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment / .3)),
                        (float) (currSizeEye * zoomAdjustment),
                        (float) (currSizeEye * zoomAdjustment));
            }
        }
    }

    /**
     * Portray dead unit
     */
    void portrayDeadSingle(BaseSingle single, Camera camera, Terrain terrain, DrawingSettings settings) {
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
        if (!DrawingUtils.drawable(drawX, drawY, INPUT_WIDTH, INPUT_HEIGHT)) return;

        // If it's drawable, draw the alive unit and potentially add some sound
        drawDeadSingle(drawX, drawY, zoomAdjustment, single, camera, settings);
        soundDeadSingle(single, camera);
    }

    /**
     * Add sound created by troop that just died
     */
    void soundDeadSingle(BaseSingle single, Camera camera) {

        // If sound effect not turned on don't play anything
        if (!audioSettings.isSoundEffect()) return;

        // Scream dead once
        if (single.isScreamDeath()) {
            // Play dead sound here
            single.setScreamDeath(false);  // Only scream death once
        }
    }

    /**
     * Dead unit drawer
     * A dead unit will most of the time have just a gray body
     */
    void drawDeadSingle(double drawX, double drawY, double zoomAdjustment, BaseSingle single, Camera camera, DrawingSettings settings) {

        // Angle of the dead troop
        double angle = camera.getDrawingAngle(single.getFacingAngle());

        // Fill with color of dead
        int[] color = DrawingConstants.COLOR_DEAD;
        fill(color[0], color[1], color[2], color[3]);

        if (single instanceof CavalrySingle) {
            shapeDrawer.cavalryShape(g,
                    drawX,
                    drawY,
                    angle,
                    currSizeCavalry[INDEX_TROOP_SIZE] * zoomAdjustment, camera);
        } else if (single instanceof PhalanxSingle) {
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    currSizePhalanx[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizePhalanx[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment, camera);
        } else if (single instanceof SwordmanSingle) {
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    currSizeSwordman[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeSwordman[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment, camera);
        } else if (single instanceof ArcherSingle) {
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    currSizeArcher[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeArcher[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment, camera);
        } else if (single instanceof BallistaSingle) {
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    currSizeBalista[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeBalista[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment, camera);
        } else if (single instanceof SlingerSingle) {
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    currSizeSlinger[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeSlinger[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment, camera);
        } else if (single instanceof SkirmisherSingle) {
            shapeDrawer.infantryShape(g,
                    drawX,
                    drawY,
                    currSizeSkirmisher[INDEX_TROOP_SIZE] * zoomAdjustment,
                    currSizeSkirmisher[INDEX_TROOP_SIMPLIED_SIZE] * zoomAdjustment, camera);
        }
    }
}
