import model.algorithms.pathfinding.Node;
import model.algorithms.pathfinding.Path;
import model.checker.EnvironmentChecker;
import model.constants.GameplayConstants;
import model.construct.Construct;
import model.enums.*;
import model.logger.Log;
import model.monitor.MonitorEnum;
import model.settings.GameSettings;
import model.surface.BaseSurface;
import model.surface.ForestSurface;
import model.map_objects.Tree;
import utils.ConfigUtils;
import ai.AIAgent;
import view.audio.AudioSpeaker;
import view.audio.AudioType;
import view.camera.BaseCamera;
import view.camera.CameraConstants;
import view.constants.ControlConstants;
import view.drawer.*;
import model.GameEnvironment;
import view.camera.TopDownCamera;
import model.projectile_objects.BaseProjectile;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;
import processing.sound.SoundFile;
import model.singles.*;
import model.units.*;
import model.utils.*;
import view.constants.DrawingConstants;
import view.components.Scrollbar;
import view.settings.AudioSettings;
import view.settings.DrawingMode;
import view.settings.DrawingSettings;
import view.settings.RenderMode;
import view.drawer.DrawingUtils;
import view.video.VideoElementPlayer;

import java.io.IOException;
import java.util.*;

public class MainSimulation extends PApplet {

    /** Screen constants */
    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    /** Drawers
     * This helps store each special shape at size to save time.
     */
    UIDrawer uiDrawer;
    ShapeDrawer shapeDrawer;
    MapDrawer mapDrawer;
    InfoDrawer infoDrawer;
    BattleSignalDrawer battleSignalDrawer;
    ObjectDrawer objectDrawer;
    SingleDrawer singleDrawer;

    /** Sound files */
    AudioSpeaker audioSpeaker;
    SoundFile backgroundMusic;

    /** Video element players */
    VideoElementPlayer videoElementPlayer;

    /** Image files */
    PImage mapTexture;

    /** Key pressed set */
    HashSet<Character> keyPressedSet;

    /** Game variables
     * Variables necessary for the running of the game
     */
    GameSettings gameSettings;
    GameEnvironment env;

    /** Camera */
    BaseCamera camera;
    double cameraRotationSpeed;
    double cameraDx;
    double cameraDy;

    /** Scrollbar, used for tuning */
    ArrayList<Scrollbar> scrollbars;

    /** Some graphical settings */
    DrawingSettings drawingSettings;
    AudioSettings audioSettings;
    int zoomCounter;
    double zoomGoal;
    int planCounter;

    /** Time recorder */
    long lastTime;
    long backEndTime;
    long graphicTime;

    /** Current playing state */
    boolean currentlyPaused;

    /** Controller variable */
    BaseUnit unitSelected;
    boolean rightClickedNotReleased;
    double rightClickActualX;
    double rightClickActualY;
    double unitEndPointX;
    double unitEndPointY;
    double unitEndAngle;
    BaseUnit closestUnit;

    /** AI agents */
    ArrayList<AIAgent> aiAgents;
    PoliticalFaction aiPoliticalFaction;
    
    public void settings() {

        // First log to initialize the logging tool
        Log.info("Initialize the log");

        // Window size
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);

        // Game settings
        gameSettings = new GameSettings();
        gameSettings.setApplyTerrainModifier(true);
        gameSettings.setBorderInwardCollision(false);  // TODO: Bugged
        gameSettings.setAllyCollision(true);
        gameSettings.setCollisionCheckingOnlyInCombat(false);
        gameSettings.setCavalryCollision(true);
        gameSettings.setEnableFlankingMechanics(true);
        gameSettings.setCountWrongFormationChanges(true);
        gameSettings.setProcessSoundBounce(false);
        gameSettings.setUseRoundedSurfaceCollision(true);
        gameSettings.setProcessUnitVision(false);
        gameSettings.setCreateAIAgent(true);

        // Graphic settings
        drawingSettings = new DrawingSettings();
        drawingSettings.setRenderMode(RenderMode.MINIMALISTIC);
        drawingSettings.setDrawEye(DrawingMode.NOT_DRAW);
        drawingSettings.setDrawWeapon(DrawingMode.DRAW);
        drawingSettings.setProduceFootage(false);
        drawingSettings.setFrameSkips(0);
        drawingSettings.setDrawSurface(false);
        drawingSettings.setSmoothCameraMovement(true);
        drawingSettings.setSmoothRotationSteps(40);
        drawingSettings.setSmoothPlanShowingSteps(100);
        drawingSettings.setDrawHeightField(true);
        drawingSettings.setDrawMapTexture(false);
        drawingSettings.setDrawSmooth(true);
        drawingSettings.setDrawDamageSustained(true);
        drawingSettings.setDrawTroopShadow(true);
        drawingSettings.setDrawSimplifiedTroopShape(true);
        drawingSettings.setDrawIcon(true);
        drawingSettings.setDrawVideoEffect(true);
        drawingSettings.setDrawUnitInfo(true);
        drawingSettings.setDrawPathfindingNodes(false);
        drawingSettings.setDrawControlArrow(false);
        drawingSettings.setDrawGameInfo(true);

        // Audio settings
        audioSettings = new AudioSettings();
        audioSettings.setBackgroundMusic(false);
        audioSettings.setSoundEffect(true);

        // Post-processing settings.
        if (!drawingSettings.isDrawSmooth()) noSmooth();
        else {
            smooth(4);
        }
    }

    public void setup() {

        /** Load graphic resources */
        // TODO: Not quite sure whether to put this alongside battle config or visual config. It makes more sense for
        //  this config to be with the map, but putting it with visual config also make sense visually if we assume
        //  that map texture.
        mapTexture = loadImage("imgs/FullMap/DemoMap2.png");

        /** Pre-processing troops */
        // Create a new game based on the input configurations.
        String gameConfig = "src/configs/game_configs/game_config.json";
        String battleConfig = "src/configs/battle_configs/ai_config_2v2.json";
        String visualConfig = "src/configs/visual_configs/visual_config.json";
        String audioConfig = "src/configs/audio_configs/audio_config.json";
        env = new GameEnvironment(gameConfig, battleConfig, gameSettings);

        // Check to make sure that the game environment is valid
        try {
            EnvironmentChecker.checkEnvironmentValid(env);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /** Keyboard setup */
        keyPressedSet = new HashSet<>();

        /** AI set up*/
        aiAgents = new ArrayList<>();
        if (gameSettings.isCreateAIAgent()) {
            try {
                aiPoliticalFaction = ConfigUtils.readPoliticalFactionFromConfig(battleConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (BaseUnit unit : env.getAliveUnits()) {
                if (unit.getPoliticalFaction() == aiPoliticalFaction) {
                    aiAgents.add(new AIAgent(unit, env));
                }
            }
        }

        /** Camera setup */
        // Calculate average position of units, and create a camera.
        double[] cameraPos = calculateAveragePositions(env.getUnits());
        camera = new TopDownCamera(cameraPos[0], cameraPos[1], INPUT_WIDTH, INPUT_HEIGHT, env.getBroadcaster());
        cameraRotationSpeed = 0;
        cameraDx = 0;
        cameraDy = 0;
        zoomGoal = camera.getZoom();

        /** Scrollbar setup */
        scrollbars = new ArrayList<>();

        /** Drawer setup */
        uiDrawer = new UIDrawer(this, camera, drawingSettings);
        shapeDrawer = new ShapeDrawer(this, camera);
        mapDrawer = new MapDrawer(this, camera);
        infoDrawer = new InfoDrawer(this);
        battleSignalDrawer = new BattleSignalDrawer(this, camera, drawingSettings);
        objectDrawer = new ObjectDrawer(this, camera, shapeDrawer, drawingSettings);
        singleDrawer = new SingleDrawer(this, env, camera, shapeDrawer, drawingSettings);

        /** Setup video element player */
        try {
            videoElementPlayer = ConfigUtils.readVideoElementConfig(
                    visualConfig, camera, this, env.getBroadcaster()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        /** Load sound files */
        // Set up audio speaker
        try {
            audioSpeaker = ConfigUtils.readAudioConfig(audioConfig, camera, this, env.getBroadcaster());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load background music
        if (audioSettings.isBackgroundMusic()) {
            try {
                backgroundMusic = ConfigUtils.createBackgroundMusicFromConfig(audioConfig, this);
                backgroundMusic.loop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Playing state of the music
        currentlyPaused = false;
    }

    /**
     * Looping method required for a Processing Applet.
     */
    public void draw() {

        /** Update the backend */
        // Record time
        lastTime = System.nanoTime();

        if (!currentlyPaused) {
            // The environment makes one step forward in processing.
            for (int i = 0; i < drawingSettings.getFrameSkips() + 1; i++) {
                env.step();
            }
        }

        if (!currentlyPaused) {
            camera.update();
        }

        // Record backend time
        backEndTime = System.nanoTime() - lastTime;

        // Process some controller variables (such as unitSelected) to ensure there is no frontend bugs.
        // Common front end bugs include processing the unit when it is already dead.
        if (env.getDeadUnits().contains(unitSelected)) {
            unitSelected = null;
        }

        /** Update some graphical elements.
         * Some graphic elements of the game is extracted and pre-processed here. These include:
         * - Camera position updates.
         * - Smooth zoom update (if enabled).
         * - Unit size optimization.
         * - Update nearest unit to mouse cursor.
         */
        // Pre-process all drawer. This pre-process is vital to short-cut calculation and optimization.
        uiDrawer.preprocess();
        shapeDrawer.preprocess();
        mapDrawer.preprocess();
        infoDrawer.preprocess();
        battleSignalDrawer.preprocess();
        objectDrawer.preprocess();
        singleDrawer.preprocess();
        for (Scrollbar scrollbar : scrollbars) {
            scrollbar.update();
        }

        if (drawingSettings.isSmoothCameraMovement()) {

            // Update the camera zoom.
            if (zoomCounter >= 0) {
                zoomCounter -= 1;
                double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / CameraConstants.ZOOM_SMOOTHEN_STEPS;
                camera.setZoom(zoom);
            }

            // Update the camera angle.
            if (keyPressed) {
                if (key == 'q') {
                    cameraRotationSpeed = CameraConstants.CAMERA_ROTATION_SPEED;
                }
                if (key == 'e') {
                    cameraRotationSpeed = -CameraConstants.CAMERA_ROTATION_SPEED;
                }
            }
            camera.setAngle(camera.getAngle() + cameraRotationSpeed);
            cameraRotationSpeed *= CameraConstants.CAMERA_ZOOM_DECELERATION_COEFFICIENT;
        }

        // Update view.camera keyboard movement
        double screenMoveAngle;
        if (keyPressed) {
            cameraDx = 0;
            cameraDy = 0;
            if (keyPressedSet.contains('a')) {
                screenMoveAngle = Math.PI + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
            if (keyPressedSet.contains('d')) {
                screenMoveAngle = camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
            if (keyPressedSet.contains('w')) {
                screenMoveAngle = Math.PI * 3 / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
            if (keyPressedSet.contains('s')) {
                screenMoveAngle = Math.PI / 2 + camera.getAngle();
                double unitX = MathUtils.quickCos((float) screenMoveAngle);
                double unitY = MathUtils.quickSin((float) screenMoveAngle);
                cameraDx += CameraConstants.CAMERA_SPEED * unitX;
                cameraDy += CameraConstants.CAMERA_SPEED * unitY;
            }
        }
        camera.move(cameraDx / camera.getZoom(), cameraDy  / camera.getZoom());
        cameraDx *= CameraConstants.CAMERA_MOVEMENT_DECELERATION_COEFFICIENT;
        cameraDy *= CameraConstants.CAMERA_MOVEMENT_DECELERATION_COEFFICIENT;

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

        /** Draw the game graphics. These include:
         * - Background
         * - Map texture and height lines
         * - Surfaces
         * - Dead troops
         * - Alive troops
         * - Objects (Catapult stones, arrows, etc.).
         * - Constructs.
         * - Path finding information (nodes, edges, etc.). These only shows for debugging purpose.
         */

        // Clear everything
        background(230);

        // Default rect mode for troops
        rectMode(CENTER);

        // Then, draw the dots that represents the height.
        if (drawingSettings.isDrawMapTexture()) {
            mapDrawer.drawMapTexture(env.getTerrain(), mapTexture);
        }
        if (drawingSettings.isDrawHeightField()) {
            mapDrawer.drawTerrainLine(env.getTerrain());
        }

        // Draw the surface.
        if (drawingSettings.isDrawSurface()) {
            for (BaseSurface surface : env.getSurfaces()) {
                int[] surfaceColor = DrawingUtils.getSurfaceColor(surface);
                fill(surfaceColor[0], surfaceColor[1], surfaceColor[2], surfaceColor[3]);
                double[][] pts = surface.getSurfaceBoundary();
                beginShape();
                for (int i = 0; i < pts.length; i++) {
                    // TODO: This is an inefficient part, the height of the object is recalculated all the time, even
                    //  though it is a very static value.
                    double[] drawingPts = camera.getDrawingPosition(pts[i][0], pts[i][1],
                            env.getTerrain().getZFromPos(pts[i][0], pts[i][1]));
                    vertex((float) drawingPts[0], (float) drawingPts[1]);
                }
                endShape(CLOSE);

                if (surface.getType() == SurfaceType.FOREST) {
                    for (Tree tree : ((ForestSurface) surface).getTrees()) {
                        int[] treeColor = DrawingConstants.TREE_COLOR;
                        double height = env.getTerrain().getZFromPos(tree.getX(), tree.getY());
                        fill(treeColor[0], treeColor[1], treeColor[2], treeColor[3]);
                        double[] drawingPosition = camera.getDrawingPosition(tree.getX(), tree.getY(),
                                height);
                        circle((float) drawingPosition[0], (float) drawingPosition[1],
                                (float) (tree.getRadius() * 2 * camera.getZoomAtHeight(height)));
                    }
                }
            }
        }

        // Dead troops
        noStroke();
        for (BaseSingle single : env.getDeadContainer()) {
            portrayDeadSingle(single);
        }

        // If space is pressed, draw the goal position.
        if (keyPressed) {
            if (key == TAB) {
                planCounter = drawingSettings.getSmoothPlanShowingSteps();
            }
        }

        if (planCounter > 0) {
            for (BaseUnit unit : env.getAliveUnits()) {
                if (unit.getState() == UnitState.MOVING) {
                    if (unit == unitSelected) continue;
                    int[] color = DrawingUtils.getFactionColor(unit.getPoliticalFaction());
                    fill(color[0], color[1], color[2], (int) (Math.min(1.0 * planCounter / 30, 0.90) * 255));
                    // TODO: Switch the draw using the current path instead of just the average position and goal
                    //  position.
                    battleSignalDrawer.drawArrowPlan(
                            unit.getAverageX(), unit.getAverageY(),
                            unit.getGoalX(), unit.getGoalY(), env.getTerrain());
                }
            }
            planCounter -= 1;
        }

        for(AIAgent agent : aiAgents){
            UnitState state= agent.getUnit().getState();
            if(state == UnitState.STANDING){
                agent.move();
            }
        }

        // Always draw arrow of selected unit
        if (unitSelected != null) {

            // Get the political faction color
            int[] color = DrawingUtils.getFactionColor(unitSelected.getPoliticalFaction());

            // If the mouse is being right clicked, the user can drag to select whether the goal would be. Visualize
            // the changed formation if the new width of the front makes sense.
            double[] actualCurrent = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
            double distance = MathUtils.quickDistance(
                    rightClickActualX, rightClickActualY, actualCurrent[0], actualCurrent[1]);
            if (rightClickedNotReleased && distance >
                    GameplayConstants.MINIMUM_WIDTH_SELECTION * unitSelected.getUnitStats().spacing) {
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
                unitEndAngle = angle - Math.PI / 2;
            } else {
                double[] endPoints = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                unitEndPointX = endPoints[0];
                unitEndPointY = endPoints[1];
                unitEndAngle = MathUtils.atan2(endPoints[1] - unitSelected.getAnchorY(), endPoints[0] - unitSelected.getAnchorX());
            }

            // Draw the arrow plan if the unit is current moving.
            // TODO: Pretty scrappy code here. It functions perfectly but it is quite hard to read.
            //  Probably should wrap this around into something like "DrawingUtils.drawPath(path)", which is a bit more
            //  clear to read.
            if (drawingSettings.isDrawControlArrow() && unitSelected.getState() == UnitState.MOVING) {
                fill(color[0], color[1], color[2], 255);
                Node prev = null;
                for (Node node : unitSelected.getPath().getNodes()) {
                    shapeDrawer.circleShape(node.getX(), node.getY(), 200 * camera.getZoomAtHeight(
                            env.getTerrain().getZFromPos(node.getX(), node.getY())));
                    if (prev != null) {
                        battleSignalDrawer.drawArrowPlan(
                                prev.getX(), prev.getY(), node.getX(), node.getY(), env.getTerrain());
                    } else {
                        battleSignalDrawer.drawArrowPlan(
                                unitSelected.getAverageX(), unitSelected.getAverageY(),
                                unitSelected.getGoalX(), unitSelected.getGoalY(), env.getTerrain());
                    }
                    prev = node;
                }
            }

            // Draw path finding
            if (drawingSettings.isDrawControlArrow()) {double[] endPoint = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                Path shortestPath = env.getGraph().getShortestPath(
                        unitSelected.getAverageX(), unitSelected.getAverageY(),
                        endPoint[0], endPoint[1], env.getConstructs());
                fill(color[0], color[1], color[2], DrawingConstants.PATH_PLANNING_ALPHA);
                Node prev = null;
                for (Node node : shortestPath.getNodes()) {
                    shapeDrawer.circleShape(node.getX(), node.getY(), 200 * camera.getZoomAtHeight(
                            env.getTerrain().getZFromPos(node.getX(), node.getY())));
                    if (prev != null) {
                        battleSignalDrawer.drawArrowPlan(prev.getX(), prev.getY(), node.getX(), node.getY(), env.getTerrain());
                    }
                    prev = node;
                }
            }
        }

        if (camera.getZoom() > CameraConstants.ZOOM_RENDER_LEVEL_TROOP) {
            // Alive troop
            for (BaseUnit unit : env.getAliveUnits()) {
                // For troops out of position, draw them individually
                for (BaseSingle single : unit.getAliveTroopsSet()) {
                    portrayAliveSingle(single, unitSelected);
                }
            }
        } else {
            // Draw unit block
            for (BaseUnit unit : env.getAliveUnits()) {
                if (unit.getNumAlives() == 0) continue;
                battleSignalDrawer.drawUnitBlock(unit, env.getTerrain());
            }
        }

        // Draw the arrow direction of the unit
        for (BaseUnit unit : env.getAliveUnits()) {
            double unitX = MathUtils.quickCos((float) unit.getAnchorAngle());
            double unitY = MathUtils.quickSin((float) unit.getAnchorAngle());
            int[] color = DrawingConstants.COLOR_GOOD_BLACK;
            fill(color[0], color[1], color[2], color[3]);
            battleSignalDrawer.drawArrowAtHeight(
                    unit.getAnchorX(), unit.getAnchorY(),
                    unit.getAnchorX() + unitX * DrawingConstants.ANCHOR_ARROW_SIZE,
                    unit.getAnchorY() + unitY * DrawingConstants.ANCHOR_ARROW_SIZE,
                    env.getTerrain().getZFromPos(unit.getAnchorX(), unit.getAnchorY()));
        }

        // Draw the objects
        ArrayList<BaseProjectile> objects = env.getUnitModifier().getProjectileHasher().getObjects();
        for (BaseProjectile obj : objects) {
            // TODO: Add probabilistic drawing here to reduce the number of arrow drawn when zooming out.
            if (obj.isAlive()) objectDrawer.drawObject(obj, env.getTerrain());
        }

        // Draw the construct.
        for (Construct construct : env.getConstructs()) {
            int[] constructColor = DrawingConstants.COLOR_GOOD_BLACK;
            fill(constructColor[0], constructColor[1], constructColor[2], 230);
            double[][] pts = construct.getBoundaryPoints();
            beginShape();
            for (int i = 0; i < pts.length; i++) {
                // TODO: This is an efficient part, the height of the object is recalculated all the time.
                double[] drawingPts = camera.getDrawingPosition(pts[i][0], pts[i][1],
                        env.getTerrain().getZFromPos(pts[i][0], pts[i][1]));
                vertex((float) drawingPts[0], (float) drawingPts[1]);
            }
            endShape(CLOSE);
        }

        // Draw the node of the graph.
        if (drawingSettings.isDrawPathfindingNodes()) {
            for (Node node : env.getGraph().getNodes()) {
                fill(245, 121, 74);
                double height = env.getTerrain().getZFromPos(node.getX(), node.getY());
                double[] drawingPts = camera.getDrawingPosition(node.getX(), node.getY(), height);
                circle((float) drawingPts[0], (float) drawingPts[1], (float) (200 * camera.getZoomAtHeight(height)));
            }
        }

        if (drawingSettings.isDrawVideoEffect()) videoElementPlayer.processElementQueue();

        /** Process the sound of the game */
        if (audioSettings.isSoundEffect()) audioSpeaker.processEvents();

        /** Draw the UI of the game. This include:
         * - Unit icons
         * - Unit information attached to the icon.
         * - Scroll bars
         * - Game stats on the bottom left.
         */
        // Draw all the unit icon
        ArrayList<BaseUnit> unitsSortedByPosition = new ArrayList<>(env.getAliveUnits());
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
                boolean isSelected = unit == unitSelected;
                boolean isAI = false;
                for (AIAgent aiAgent : aiAgents) {
                    if (unit == aiAgent.getUnit()) {
                        isAI = true;
                        break;
                    }
                }
                uiDrawer.drawUnitBanner(unit, isSelected, isAI);

            }
        }

        if (drawingSettings.isDrawUnitInfo()) {
            for (BaseUnit unit : unitsSortedByPosition) {
                if (unit.getNumAlives() == 0) continue;
                // Write all the interesting counters here.
                StringBuilder s = new StringBuilder();
                s.append("Unit Type: " + unit.getUnitType().toString() + "\n");
                s.append("Strength:  " + unit.getNumAlives() + "/" + unit.getTroops().size() + "\n");
                s.append("Stamina:   " + String.format("%.2f%%", unit.getStamina() * 100) + "\n");
                s.append("Morale:    " + String.format("%.2f%%", unit.getMorale() * 100));
                // Draw the info box
                double[] drawingPoints = camera.getDrawingPosition(
                        unit.getAverageX(), unit.getAverageY(), unit.getAverageZ());
                infoDrawer.drawTextBox(s.toString(), drawingPoints[0] + 36, drawingPoints[1] - 22, 200);
            }
        }

        // Scroll bars
        for (Scrollbar scrollbar : scrollbars) {
            scrollbar.display();
        }

        // Process graphics
        fill(0, 0, 0);
        graphicTime = System.nanoTime() - lastTime - backEndTime;

        // Write all the interesting counters here.
        if (drawingSettings.isDrawGameInfo()) {
            StringBuilder s = new StringBuilder();
            s.append(env.getMonitor().getCounterString(
                    new MonitorEnum[] {
                            MonitorEnum.COLLISION_TROOPS,
                            MonitorEnum.COLLISION_TROOP_AND_TERRAIN,
                            MonitorEnum.COLLISION_TROOP_AND_CONSTRUCT,
                            MonitorEnum.COLLISION_TROOP_AND_TREE,
                            MonitorEnum.COLLISION_OBJECT,
                    }
            ));
            s.append(env.getMonitor().getTotalCounterString(
                    new MonitorEnum[] {
                            MonitorEnum.WRONG_FORMATION_CHANGES,
                    }
            ));
            s.append("Camera shake level              : " + String.format("%.2f", camera.getCameraShakeLevel()) + "\n");
            s.append("Zoom level                      : " + String.format("%.2f", camera.getZoom()) + "\n");
            s.append("Backends                        : " + String.format("%.2f", 1.0 * backEndTime / 1000000) + "ms\n");
            s.append("Graphics                        : " + String.format("%.2f", 1.0 * graphicTime / 1000000) + "ms\n");
            s.append("FPS                             : " + String.format("%.2f", 1.0 * 1000000000 / (graphicTime + backEndTime)));
            infoDrawer.drawTextBox(s.toString(), 5, INPUT_HEIGHT - 5, 400);
        }

        // Pause / Play Button
        if (!currentlyPaused) {
            uiDrawer.pauseButton(INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        } else {
            uiDrawer.playButton(INPUT_WIDTH - 50, INPUT_HEIGHT - 50, 40);
        }

        /** Produce the image of the game */
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

        if (mouseButton == LEFT) {
            // Check distance, only allow unit assignment if distance to mouse is smaller than certain number.
            // TODO: It would be better to actually check against the Unit Bounding box for a more accurate collision
            //  checking.
            boolean isAIAgent = false;
            for (AIAgent aiAgent : aiAgents) {
                if (closestUnit == aiAgent.getUnit()) {
                    isAIAgent = true;
                    break;
                }
            }
            double[] screenPos = camera.getDrawingPosition(
                    closestUnit.getAverageX(), closestUnit.getAverageY(), closestUnit.getAverageZ());
            if (!isAIAgent && MathUtils.squareDistance(mouseX, mouseY, screenPos[0], screenPos[1])
                    < ControlConstants.UNIT_ASSIGNMENT_MOUSE_SQ_DISTANCE) {
                unitSelected = closestUnit;
            } else {
                unitSelected = null;
            }
            audioSpeaker.broadcastOverlaySound(AudioType.LEFT_CLICK);
        } else if (mouseButton == RIGHT) {
            audioSpeaker.broadcastOverlaySound(AudioType.RIGHT_CLICK);
            if (unitSelected != null) {
                // If the unit currently selected is in panic mode, the player does not have any control over them.
                if (unitSelected.getState() == UnitState.ROUTING) {
                    // During routing, unit is uncontrollable. We shall apply no command here.
                    return;
                }

                // Check distance
                if (unitSelected instanceof ArcherUnit) {
                    // Convert closest unit to click
                    double[] screenPos = camera.getDrawingPosition(
                            closestUnit.getAverageX(),
                            closestUnit.getAverageY());

                    // If it's an archer unit, check the faction and distance from the closest unit from camera
                    if (closestUnit.getPoliticalFaction() != unitSelected.getPoliticalFaction() &&
                            MathUtils.squareDistance(mouseX, mouseY, screenPos[0], screenPos[1]) <
                                    CameraConstants.SQUARE_CLICK_ATTACK_DISTANCE) {
                        ((ArcherUnit) unitSelected).setUnitFiredAt(closestUnit);
                        if (unitSelected.getState() == UnitState.MOVING) {
                            unitSelected.moveFormationKeptTo(
                                    unitSelected.getAnchorX(), unitSelected.getAnchorY(), unitSelected.getAnchorAngle());
                        }
                    } else {
                        if (GameplayUtils.checkIfUnitCanMoveTowards(
                                unitEndPointX, unitEndPointY, env.getConstructs())) {
                            unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                        }
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
                        if (GameplayUtils.checkIfUnitCanMoveTowards(
                                unitEndPointX, unitEndPointY, env.getConstructs())) {
                            unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                        }
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
                        if (GameplayUtils.checkIfUnitCanMoveTowards(
                                unitEndPointX, unitEndPointY, env.getConstructs())) {
                            unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                        }
                        ((CatapultUnit) unitSelected).setUnitFiredAt(null);
                    }
                } else {
                    if (GameplayUtils.checkIfUnitCanMoveTowards(
                            unitEndPointX, unitEndPointY, env.getConstructs())) {
                        unitSelected.moveFormationKeptTo(unitEndPointX, unitEndPointY, unitEndAngle);
                    }
                }
                double[] actualCurrent = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                double distance = MathUtils.quickDistance(
                        rightClickActualX, rightClickActualY, actualCurrent[0], actualCurrent[1]);
                if (distance > GameplayConstants.MINIMUM_WIDTH_SELECTION * unitSelected.getUnitStats().spacing) {
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
            zoomCounter = CameraConstants.ZOOM_SMOOTHEN_STEPS;
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'c') {
            drawingSettings.setDrawTroopInDanger(!drawingSettings.isDrawTroopInDanger());
        }
        keyPressedSet.add(key);
    }

    @Override
    public void keyReleased() {
        keyPressedSet.remove(key);
    }

    /**
     * Portray alive troop.
     */
    void portrayAliveSingle(BaseSingle single, BaseUnit unitSelected) {

        // Draw all the object sticking to the individual
        HashMap<BaseProjectile, Integer> carriedObjects = single.getCarriedObjects();
        for (BaseProjectile obj : carriedObjects.keySet()) {
            objectDrawer.drawObjectCarriedByTroop(carriedObjects.get(obj), obj, single);
        }

        // Draw the alive single itself
        singleDrawer.drawAliveSingle(single, unitSelected == single.getUnit());
    }

    /**
     * Portray dead unit
     */
    void portrayDeadSingle(BaseSingle single) {
        singleDrawer.drawDeadSingle(single);
    }

    private double[] calculateAveragePositions(ArrayList<BaseUnit> units) {
        double sumX = 0;
        double sumY = 0;
        int count = 0;
        for (BaseUnit unit : units) {
            sumX += unit.getAnchorX() * unit.getNumAlives();
            sumY += unit.getAnchorY() * unit.getNumAlives();
            count += unit.getNumAlives();
        }
        return new double[] {
                sumX / count, sumY / count
        };
    }

    public static void main(String[] args){
        PApplet.main("MainSimulation");
    }
}
