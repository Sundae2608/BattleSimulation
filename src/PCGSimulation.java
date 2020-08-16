import controller.tunable.CustomAssigner;
import model.algorithms.pathfinding.*;
import model.terrain.Terrain;
import model.utils.MathUtils;
import model.utils.MapGenerationUtils;
import model.utils.Triplet;
import processing.core.PApplet;
import processing.event.MouseEvent;
import view.camera.BaseCamera;
import view.camera.CameraConstants;
import view.camera_hex.HexCamera;
import view.constants.DrawingConstants;
import view.drawer.InfoDrawer;
import view.drawer.MapDrawer;
import view.drawer.UIDrawer;
import view.drawer.components.Scrollbar;
import view.settings.DrawingSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PCGSimulation extends PApplet {

    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    private final static int INPUT_TOP_X = 0;
    private final static int INPUT_TOP_Y = 0;
    private final static int INPUT_DIV = 400;
    private final static int INPUT_NUM_X = 50;
    private final static int INPUT_NUM_Y = 50;

    private final static int NUM_HEX_RADIUS = 7;
    private final static double HEX_RADIUS = 300;
    private final static double HEX_JIGGLE = 30;
    private final static double HEX_CENTER_X = INPUT_TOP_X + INPUT_NUM_X * INPUT_DIV / 2;
    private final static double HEX_CENTER_Y = INPUT_TOP_Y + INPUT_NUM_Y * INPUT_DIV / 2;

    private final static double PT_SCALE = 0.9;
    private final static double SWAP_PROBABILITY = 0.10;

    private final static double SCALING_EXPONTENTIAL = 1.1;
    private final static double BASE_SCALE_DIST = 600;

    // Drawing settings
    DrawingSettings drawingSettings;

    // Key and mouse pressed set
    HashSet<Character> keyPressedSet;

    // Drawer
    UIDrawer uiDrawer;
    MapDrawer mapDrawer;
    InfoDrawer infoDrawer;

    // Camera
    BaseCamera camera;
    double cameraRotationSpeed;
    double cameraDx;
    double cameraDy;
    int zoomCounter;
    double zoomGoal;
    ArrayList<Scrollbar> scrollbars;

    // Terrain
    Terrain terrain;

    // Hexagonal points
    Graph graph;
    ArrayList<Triangle> triangles;
    HashSet<Polygon> polygons;
    Polygon mergedPolygon;

    public void settings() {
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);
        drawingSettings = new DrawingSettings();

        // Drawing settings
        smooth(3);
    }

    public void setup() {
        // Set up terrain.
        terrain = new Terrain(INPUT_TOP_X, INPUT_TOP_Y, INPUT_DIV, INPUT_NUM_X, INPUT_NUM_Y);

        // Set up camera.
        camera = new HexCamera(
                INPUT_TOP_X + INPUT_NUM_X * INPUT_DIV / 2,
                INPUT_TOP_Y + INPUT_NUM_Y * INPUT_DIV / 2,
                INPUT_WIDTH, INPUT_HEIGHT);
        camera.setZoom(0.10);
        cameraRotationSpeed = 0;
        cameraDx = 0;
        cameraDy = 0;

        // Set up zoom.
        zoomGoal = camera.getZoom();  // To ensure consistency

        /** Scrollbar setup */
        scrollbars = new ArrayList<>();
        scrollbars.add(new Scrollbar("Phi angle",
                INPUT_WIDTH - 300, 30, 280, 20,
                ((HexCamera) camera).getPhiAngle(), Math.PI / 24, Math.PI * 11 / 24, this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        ((HexCamera) camera).setPhiAngle(value);
                    }
                }));

        // Set up drawer
        uiDrawer = new UIDrawer(this, camera, drawingSettings);
        mapDrawer = new MapDrawer(this, camera);
        infoDrawer = new InfoDrawer(this);

        // Set of keys pressed.
        keyPressedSet = new HashSet<>();

        // Generate a set of points.
        graph = new Graph();
        HashSet<Triplet<Integer, Integer, Integer>> hexIndices = new HashSet<>();
        for (int i = 0; i < NUM_HEX_RADIUS; i++) {
            for (Triplet triplet : MathUtils.getHexagonalIndicesRingAtOffset(i)) {
                hexIndices.add(triplet);
            }
        }
        int i = 0;
        HashMap<Triplet<Integer, Integer, Integer>, Node> nodeMap = new HashMap<>();
        for (Triplet<Integer, Integer, Integer> triplet : hexIndices) {
            // Add the node to the graph
            double[] pt = MathUtils.generateOffsetBasedOnHexTripletIndices(triplet.x, triplet.y, triplet.z, HEX_RADIUS);
            Node node = new Node(pt[0] + HEX_CENTER_X, pt[1] + HEX_CENTER_Y);
            graph.addNodeWithIndex(node, i);

            // Add the node to the node map, this will help us identify adjacent node later.
            nodeMap.put(triplet, node);
            i++;
        }

        for (Triplet t : nodeMap.keySet()) {
            // System.out.println(t.x.toString() + ", " + t.y.toString() + ", " + t.z.toString());
        }

        // Connect nodes that are adjacent to each other.
        for (Triplet<Integer, Integer, Integer> t : hexIndices) {
            for (Triplet<Integer, Integer, Integer> adjacentTriplet : MathUtils.generateAdjacentHexagonalTriplets(t)) {
                if (nodeMap.containsKey(adjacentTriplet)) {
                    graph.connectNode(nodeMap.get(t), nodeMap.get(adjacentTriplet));
                }
            }
        }

        // Jiggle the nodes
        for (Node node : graph.getNodes()) {
            double[] newPt = MathUtils.polarJiggle(node.getX(), node.getY(), HEX_JIGGLE);
            node.setX(newPt[0]);
            node.setY(newPt[1]);
        }

        /**
         * Create triangles out of points. This process will be split into 4 parts.
         * + Create up triangles for top half
         * + Create up triangles for bottom half
         * + Create down triangles for top half
         * + create down triangles for bottom half
         */
        int numPoints = NUM_HEX_RADIUS;
        triangles = new ArrayList<>();
        HashMap<Triplet, Triangle> upTriangleMap = new HashMap<>();
        HashMap<Triplet, Triangle> downTriangleMap = new HashMap<>();
        HashMap<Node, HashSet<Triangle>> nodeToTriangleMap = new HashMap<>();

        Triplet<Integer, Integer, Integer> curr;
        Triplet<Integer, Integer, Integer> topLeftTriplet = new Triplet<>(0, NUM_HEX_RADIUS - 1, -NUM_HEX_RADIUS + 1);
        for (int row = 0; row < NUM_HEX_RADIUS - 1; row++) {
            for (int col = 0; col < numPoints; col++) {
                // Up triangle has the following indices relative to currTriplet
                //          (_, _, _)
                //          /       \
                // (-1, _, +1) ---- (_, -1, +1)
                curr = new Triplet<>(topLeftTriplet.x - row + col, topLeftTriplet.y - col, topLeftTriplet.z + row);

                Triplet<Integer, Integer, Integer> t1 = curr;
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x-1, curr.y, curr.z+1);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y-1, curr.z+1);
                Triangle triangle;
                if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                    triangle = new Triangle(nodeMap.get(t1), nodeMap.get(t2), nodeMap.get(t3));
                    triangles.add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t1))) {
                        nodeToTriangleMap.put(nodeMap.get(t1), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t1)).add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t2))) {
                        nodeToTriangleMap.put(nodeMap.get(t2), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t2)).add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t3))) {
                        nodeToTriangleMap.put(nodeMap.get(t3), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t3)).add(triangle);
                    upTriangleMap.put(curr, triangle);
                }
            }
            numPoints++;
        }

        topLeftTriplet = new Triplet<>(0, NUM_HEX_RADIUS - 1, -NUM_HEX_RADIUS + 1);
        for (int row = 0; row < NUM_HEX_RADIUS - 1; row++) {
            for (int col = 0; col < numPoints - 1; col++) {
                // Down triangle has the following indices relative to currTriplet
                // (_, _, _) ---- (+1, -1, _)
                //        \         /
                //        (_, -1, +1)
                curr = new Triplet<>(topLeftTriplet.x - row + col, topLeftTriplet.y - col, topLeftTriplet.z + row);

                Triplet<Integer, Integer, Integer> t1 = curr;
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x+1, curr.y-1, curr.z);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y-1, curr.z+1);
                Triangle triangle;
                if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                    triangle = new Triangle(nodeMap.get(t1), nodeMap.get(t2), nodeMap.get(t3));
                    triangles.add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t1))) {
                        nodeToTriangleMap.put(nodeMap.get(t1), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t1)).add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t2))) {
                        nodeToTriangleMap.put(nodeMap.get(t2), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t2)).add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t3))) {
                        nodeToTriangleMap.put(nodeMap.get(t3), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t3)).add(triangle);
                    downTriangleMap.put(curr, triangle);
                }
            }
            numPoints++;
        }

        topLeftTriplet = new Triplet<>(0 - NUM_HEX_RADIUS + 1, NUM_HEX_RADIUS - 1, 0);
        for (int row = 0; row < NUM_HEX_RADIUS - 1; row++) {
            for (int col = 0; col < numPoints; col++) {
                // Up triangle has the following indices relative to currTriplet
                //          (_, _, _)
                //          /       \
                // (-1, _, +1)      (_, -1, +1)
                curr = new Triplet<>(topLeftTriplet.x + col, topLeftTriplet.y - row - col, topLeftTriplet.z + row);

                Triplet<Integer, Integer, Integer> t1 = curr;
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x-1, curr.y, curr.z+1);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y-1, curr.z+1);
                if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                    Triangle triangle;
                    if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                        triangle = new Triangle(nodeMap.get(t1), nodeMap.get(t2), nodeMap.get(t3));
                        triangles.add(triangle);
                        if (!nodeToTriangleMap.containsKey(nodeMap.get(t1))) {
                            nodeToTriangleMap.put(nodeMap.get(t1), new HashSet<>());
                        }
                        nodeToTriangleMap.get(nodeMap.get(t1)).add(triangle);
                        if (!nodeToTriangleMap.containsKey(nodeMap.get(t2))) {
                            nodeToTriangleMap.put(nodeMap.get(t2), new HashSet<>());
                        }
                        nodeToTriangleMap.get(nodeMap.get(t2)).add(triangle);
                        if (!nodeToTriangleMap.containsKey(nodeMap.get(t3))) {
                            nodeToTriangleMap.put(nodeMap.get(t3), new HashSet<>());
                        }
                        nodeToTriangleMap.get(nodeMap.get(t3)).add(triangle);
                        upTriangleMap.put(curr, triangle);
                    }
                }
            }
            numPoints--;
        }

        topLeftTriplet = new Triplet<>(0 - NUM_HEX_RADIUS + 1, NUM_HEX_RADIUS - 1, 0);
        for (int row = 0; row < NUM_HEX_RADIUS - 1; row++) {
            for (int col = 0; col < numPoints - 1; col++) {
                // Down triangle has the following indices relative to currTriplet
                // (_, _, _) ---- (+1, -1, _)
                //        \         /
                //        (_, -1, +1)
                curr = new Triplet<>(topLeftTriplet.x - row + col, topLeftTriplet.y - col, topLeftTriplet.z + row);

                Triplet<Integer, Integer, Integer> t1 = curr;
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x+1, curr.y-1, curr.z);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y-1, curr.z+1);
                Triangle triangle;
                if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                    triangle = new Triangle(nodeMap.get(t1), nodeMap.get(t2), nodeMap.get(t3));
                    triangles.add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t1))) {
                        nodeToTriangleMap.put(nodeMap.get(t1), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t1)).add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t2))) {
                        nodeToTriangleMap.put(nodeMap.get(t2), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t2)).add(triangle);
                    if (!nodeToTriangleMap.containsKey(nodeMap.get(t3))) {
                        nodeToTriangleMap.put(nodeMap.get(t3), new HashSet<>());
                    }
                    nodeToTriangleMap.get(nodeMap.get(t3)).add(triangle);
                    downTriangleMap.put(curr, triangle);
                }
            }
            numPoints++;
        }

        /**
         * Perform node swapping of two adjacent triangles
         */
        // Random node swapping.
        // TODO: Add a condition to stop the swap if the the swap would create overlapping geometry.
        for (Triplet<Integer, Integer, Integer> t : upTriangleMap.keySet()) {
            Triplet<Integer, Integer, Integer> downCandidate1 = t;
            if (MathUtils.randDouble(0, 1) < SWAP_PROBABILITY && downTriangleMap.containsKey(downCandidate1)) {
                MapGenerationUtils.swapTriangleEdges(upTriangleMap.get(t), downTriangleMap.get(downCandidate1), nodeToTriangleMap);
            }
            Triplet<Integer, Integer, Integer> downCandidate2 = new Triplet<>(t.x - 1, t.y + 1, t.z);
            if (MathUtils.randDouble(0, 1) < SWAP_PROBABILITY && downTriangleMap.containsKey(downCandidate2)) {
                MapGenerationUtils.swapTriangleEdges(upTriangleMap.get(t), downTriangleMap.get(downCandidate2), nodeToTriangleMap);
            }
            Triplet<Integer, Integer, Integer> downCandidate3 = new Triplet<>(t.x - 1, t.y, t.z + 1);
            if (MathUtils.randDouble(0, 1) < SWAP_PROBABILITY && downTriangleMap.containsKey(downCandidate3)) {
                MapGenerationUtils.swapTriangleEdges(upTriangleMap.get(t), downTriangleMap.get(downCandidate3), nodeToTriangleMap);
            }
        }

        /**
         * Geometric scaling from the center.
         * A city typically has a very dense and small area in the center, followed by very sparse geometry further away
         * Therefore, we should scale so that city center appears in smaller block, while the surround blocks get
         * increasingly bigger
         */
        for (Node node : graph.getNodes()) {
            double dist = MathUtils.quickDistance(HEX_CENTER_X, HEX_CENTER_Y, node.getX(), node.getY());
            double[] newPt = MathUtils.scalePoint(new double[] {HEX_CENTER_X, HEX_CENTER_Y}, node.getPt(), Math.max(Math.log(dist / BASE_SCALE_DIST), 1));
            node.setX(newPt[0]);
            node.setY(newPt[1]);
        }

        /**
         * Remove nodes to create bigger lumps of stuffs.
         */
        // First, convert them all into polygons
        HashMap<Node, HashSet<Polygon>> nodeToPolygonMap = new HashMap<>();
        polygons = new HashSet<>();
        for (Triangle triangle : triangles) {
            polygons.add(new Polygon(triangle));
        }
        for (Polygon polygon : polygons) {
            for (Node node : polygon.getNodes()) {
                if (!nodeToPolygonMap.containsKey(node)) nodeToPolygonMap.put(node, new HashSet<>());
                nodeToPolygonMap.get(node).add(polygon);
            }
        }

        // Pick a random point to delete from the mix
        Node removeNode = null;
        for (Node node : nodeToPolygonMap.keySet()) {
            removeNode = node;
            mergedPolygon = MapGenerationUtils.mergeMultiplePolygons(new ArrayList<>(nodeToPolygonMap.get(removeNode)));
            break;
        }

        // Remove the destroyed node
        for (Node node : mergedPolygon.getNodes()) {
            for (Polygon removePolygon : nodeToPolygonMap.get(removeNode)) {
                nodeToPolygonMap.get(node).remove(removePolygon);
            }
            nodeToPolygonMap.get(node).add(mergedPolygon);
        }
        nodeToPolygonMap.remove(removeNode);
    }

    public void draw() {

        // Perform some backend update before drawing
        for (Scrollbar scrollbar : scrollbars) {
            scrollbar.update();
        }

        // Clear everything
        background(230);

        // Update the camera zoom.
        if (zoomCounter >= 0) {
            zoomCounter -= 1;
            double zoom = zoomGoal + (camera.getZoom() - zoomGoal) * zoomCounter / CameraConstants.ZOOM_SMOOTHEN_STEPS;
            camera.setZoom(zoom);
        }

        // Update the camera rotation.
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

        // Update the camera translation.
        if (keyPressed) {
            double screenMoveAngle;
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

        // Drawing terrain line
        mapDrawer.drawTerrainLine(terrain);

        // Draw triangles
        int[] color = DrawingConstants.POLYGON_COLOR;
        strokeWeight((float) (10 * camera.getZoom()));
        stroke(color[0], color[1], color[2], color[3]);
        noFill();
        beginShape(LINES);
        for (Polygon polygon : polygons) {
            double[] centerOfMass = polygon.getCenterOfMass();
            for (Edge edge : polygon.getEdges()) {
                double[] pt1 = new double[] {edge.getNode1().getX(), edge.getNode1().getY()};
                double[] pt2 = new double[] {edge.getNode2().getX(), edge.getNode2().getY()};
                pt1 = MathUtils.scalePoint(centerOfMass, pt1, PT_SCALE);
                pt2 = MathUtils.scalePoint(centerOfMass, pt2, PT_SCALE);
                pt1 = camera.getDrawingPosition(pt1[0], pt1[1], terrain.getHeightFromPos(pt1[0], pt1[1]));
                pt2 = camera.getDrawingPosition(pt2[0], pt2[1], terrain.getHeightFromPos(pt2[0], pt2[1]));
                vertex((float) pt1[0], (float) pt1[1]);
                vertex((float) pt2[0], (float) pt2[1]);
            }
        }
        endShape();
        noStroke();

        // Draw merged polygons
        color = DrawingConstants.MERGED_POLYGON_COLOR;
        strokeWeight((float) (10 * camera.getZoom()));
        stroke(color[0], color[1], color[2], color[3]);
        noFill();
        beginShape(LINES);
        double[] centerOfMass = mergedPolygon.getCenterOfMass();
        for (Edge edge : mergedPolygon.getEdges()) {
            double[] pt1 = new double[] {edge.getNode1().getX(), edge.getNode1().getY()};
            double[] pt2 = new double[] {edge.getNode2().getX(), edge.getNode2().getY()};
            pt1 = MathUtils.scalePoint(centerOfMass, pt1, PT_SCALE);
            pt2 = MathUtils.scalePoint(centerOfMass, pt2, PT_SCALE);
            pt1 = camera.getDrawingPosition(pt1[0], pt1[1], terrain.getHeightFromPos(pt1[0], pt1[1]));
            pt2 = camera.getDrawingPosition(pt2[0], pt2[1], terrain.getHeightFromPos(pt2[0], pt2[1]));
            vertex((float) pt1[0], (float) pt1[1]);
            vertex((float) pt2[0], (float) pt2[1]);
        }
        endShape();
        noStroke();

        // Draw pts
        color = DrawingConstants.NODE_COLOR;
        fill(color[0], color[1], color[2], color[3]);
        for (Node node : graph.getNodes()) {
            double[] drawingPt = camera.getDrawingPosition(
                    node.getX(), node.getY(), terrain.getHeightFromPos(node.getX(), node.getY()));
            circle((float) drawingPt[0], (float) drawingPt[1], (float) (DrawingConstants.NODE_RADIUS * camera.getZoom()));
        }

        // Draw zoom information
        StringBuilder s = new StringBuilder();
        s.append("Camera shake level              : " + String.format("%.2f", camera.getCameraShakeLevel()) + "\n");
        s.append("Zoom level                      : " + String.format("%.2f", camera.getZoom()) + "\n");
        infoDrawer.drawTextBox(s.toString(), 5, INPUT_HEIGHT - 5, 400);

        // Scroll bars
        for (Scrollbar scrollbar : scrollbars) {
            scrollbar.display();
        }
    }

    @Override
    public void keyPressed() {
        keyPressedSet.add(key);
    }

    @Override
    public void keyReleased() {
        keyPressedSet.remove(key);
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        float scrollVal = event.getCount();
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

    public static void main(String... args){
        PApplet.main("PCGSimulation");
    }
}
