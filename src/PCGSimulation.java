import controller.tunable.CustomAssigner;
import model.algorithms.geometry.Edge;
import model.algorithms.geometry.Polygon;
import model.algorithms.geometry.PolygonSystem;
import model.algorithms.pathfinding.*;
import model.settings.MapGenerationSettings;
import model.terrain.Terrain;
import model.utils.MathUtils;
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

import java.util.*;

public class PCGSimulation extends PApplet {

    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    private final static int INPUT_TOP_X = 0;
    private final static int INPUT_TOP_Y = 0;
    private final static int INPUT_DIV = 400;
    private final static int INPUT_NUM_X = 50;
    private final static int INPUT_NUM_Y = 50;

    private final static int NUM_HEX_RADIUS = 20;
    private final static double HEX_RADIUS = 300;
    private final static double HEX_JIGGLE = 120;
    private final static double HEX_CENTER_X = INPUT_TOP_X + INPUT_NUM_X * INPUT_DIV / 2;
    private final static double HEX_CENTER_Y = INPUT_TOP_Y + INPUT_NUM_Y * INPUT_DIV / 2;
    private final static double HEX_CENTER_JIGGLE = 1800;

    private final static double PT_SCALE = 0.95;
    private final static double SWAP_PROBABILITY = 0.10;

    private final static double LOG_BASE = 20.0;
    private final static double BASE_SCALE_DIST = 7000;

    // Config for the number of nodes
    private final static double NUM_NODES_CITY_CENTER = 16;

    // Drawing settings
    DrawingSettings drawingSettings;
    MapGenerationSettings mapGenerationSettings;

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

    // Polygon system
    Graph graph;
    PolygonSystem polygonSystem;

    public void settings() {
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);
        drawingSettings = new DrawingSettings();

        mapGenerationSettings = new MapGenerationSettings();
        mapGenerationSettings.setPointExtension(true);

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
        ArrayList<Polygon> triangles = new ArrayList<>();
        HashMap<Triplet, Polygon> upTriangleMap = new HashMap<>();
        HashMap<Triplet, Polygon> downTriangleMap = new HashMap<>();

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
                Polygon triangle;
                if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                    HashSet<Edge> edges = new HashSet<>();
                    edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t2)));
                    edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t3)));
                    edges.add(new Edge(nodeMap.get(t2), nodeMap.get(t3)));
                    triangle = new Polygon(edges);
                    triangles.add(triangle);
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
                Polygon triangle;
                if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                    HashSet<Edge> edges = new HashSet<>();
                    edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t2)));
                    edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t3)));
                    edges.add(new Edge(nodeMap.get(t2), nodeMap.get(t3)));
                    triangle = new Polygon(edges);
                    triangles.add(triangle);
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
                    Polygon triangle;
                    if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                        HashSet<Edge> edges = new HashSet<>();
                        edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t2)));
                        edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t3)));
                        edges.add(new Edge(nodeMap.get(t2), nodeMap.get(t3)));
                        triangle = new Polygon(edges);
                        triangles.add(triangle);
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
                Polygon triangle;
                if (nodeMap.containsKey(t1) && nodeMap.containsKey(t2) && nodeMap.containsKey(t3)) {
                    HashSet<Edge> edges = new HashSet<>();
                    edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t2)));
                    edges.add(new Edge(nodeMap.get(t1), nodeMap.get(t3)));
                    edges.add(new Edge(nodeMap.get(t2), nodeMap.get(t3)));
                    triangle = new Polygon(edges);
                    triangles.add(triangle);
                    downTriangleMap.put(curr, triangle);
                }
            }
            numPoints++;
        }

        /** Scale the points further */
        // Point extension
        if (mapGenerationSettings.isPointExtension()) {
            for (Node node : graph.getNodes()) {
                double dist = MathUtils.quickDistance(HEX_CENTER_X, HEX_CENTER_Y, node.getX(), node.getY());
                double[] newPt = MathUtils.scalePoint(
                        new double[]{HEX_CENTER_X, HEX_CENTER_Y},
                        node.getPt(),
                        Math.max(Math.log(dist / BASE_SCALE_DIST) / Math.log(LOG_BASE) + 1, 1));
                node.setX(newPt[0]);
                node.setY(newPt[1]);
            }
        }

        /**
         * Create a new polygon system out of the triangles and perform random edge swapping
         */
        polygonSystem = new PolygonSystem();
        for (Polygon polygon : triangles) {
            polygonSystem.addPolygon(polygon);
        }

        // Edge swapping
        for (Object o : polygonSystem.getEdges().toArray()) {
            Edge edge = (Edge) o;
            if (polygonSystem.getAdjacentPolygon(edge) != null &&
                polygonSystem.getAdjacentPolygon(edge).size() == 2 &&
                MathUtils.randDouble(0.0, 1.0) < SWAP_PROBABILITY) {
                Polygon p1 = (Polygon) polygonSystem.getAdjacentPolygon(edge).toArray()[0];
                Polygon p2 = (Polygon) polygonSystem.getAdjacentPolygon(edge).toArray()[1];
                polygonSystem.swapTriangleEdge(p1, p2);
            }
        }

        /**
         * Combine nodes together according to a certain city layout.
         */
        double[] centerPt = MathUtils.polarJiggle(HEX_CENTER_X, HEX_CENTER_Y, HEX_CENTER_JIGGLE);
        ArrayList<Node> nodeList = new ArrayList<>(polygonSystem.getNodes());
        Collections.sort(nodeList, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double dist1 = MathUtils.quickDistance(o1.getX(), o1.getY(), centerPt[0], centerPt[1]);
                double dist2 = MathUtils.quickDistance(o2.getX(), o2.getY(), centerPt[0], centerPt[1]);
                if (dist1 < dist2) {
                    return -1;
                } else if (dist1 == dist2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        // Make city center.
        HashSet<Polygon> mergedPolygonSet = new HashSet<>();
        HashSet<Polygon> polygonSet = new HashSet<>();
        for (i = 0; i < NUM_NODES_CITY_CENTER; i++) {
            for (Polygon polygon : polygonSystem.getAdjacentPolygon(nodeList.get(i))) {
                polygonSet.add(polygon);
            }
        }
        mergedPolygonSet.add(polygonSystem.mergeMultiplePolygons(new ArrayList<>(polygonSet)));

        // For the rest of the land, just keep merging any point that has not been parts of a polygon merged.
        nodeList = new ArrayList<>(polygonSystem.getNodes());
        Collections.sort(nodeList, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double dist1 = MathUtils.quickDistance(o1.getX(), o1.getY(), centerPt[0], centerPt[1]);
                double dist2 = MathUtils.quickDistance(o2.getX(), o2.getY(), centerPt[0], centerPt[1]);
                if (dist1 < dist2) {
                    return -1;
                } else if (dist1 == dist2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (Node node : nodeList) {
            boolean adjacentNode = false;
            for (Polygon polygon : mergedPolygonSet) {
                if (polygon.getNodes().contains(node)) {
                    adjacentNode = true;
                    break;
                }
            }
            if (!adjacentNode) {
                mergedPolygonSet.add(polygonSystem.mergeMultiplePolygons(
                        new ArrayList<>(polygonSystem.getAdjacentPolygon(node)))
                );
            }
        }

        // Randomly merge the remaining triangles
        ArrayList<Polygon> remainingPolygons = new ArrayList<>(polygonSystem.getPolygons());
        for (Polygon polygon : remainingPolygons) {
            if (polygon.getNodes().size() == 3) {
                double maxEdgeLength = 0;
                Edge maxEdge = null;
                for (Edge edge : polygon.getEdges()) {
                    if (edge.getLength() > maxEdgeLength) {
                        maxEdgeLength = edge.getLength();
                        maxEdge = edge;
                    }
                }

                if (maxEdge == null) continue;
                if (polygonSystem.getAdjacentPolygon(maxEdge) == null) continue;
                mergedPolygonSet.add(polygonSystem.mergeMultiplePolygons(
                        new ArrayList<>(polygonSystem.getAdjacentPolygon(maxEdge)))
                );
            }
        }

        // TODO: Draw roads.
        // TODO: Add random rectangles.
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
        noStroke();
        fill(color[0], color[1], color[2], color[3]);
        for (Polygon t : polygonSystem.getPolygons()) {
            beginShape();
            double[] centerOfMass = t.getCenterOfMass();
            ArrayList<Node> nodes = t.getOrderedNodes();
            for (Node node : nodes) {
                double[] pt = new double[] {node.getX(), node.getY()};
                pt = MathUtils.scalePoint(centerOfMass, pt, PT_SCALE);
                pt = camera.getDrawingPosition(pt[0], pt[1], terrain.getHeightFromPos(pt[0], pt[1]));
                vertex((float) pt[0], (float) pt[1]);
            }
            endShape(CLOSE);
        }

        // Draw pts
        color = DrawingConstants.NODE_COLOR;
        for (Node node : polygonSystem.getNodes()) {
            fill(color[0], color[1], color[2], color[3]);
            double[] drawingPt = camera.getDrawingPosition(
                    node.getX(), node.getY(), terrain.getHeightFromPos(node.getX(), node.getY()));
            circle((float) drawingPt[0], (float) drawingPt[1], (float) (DrawingConstants.NODE_RADIUS * camera.getZoom()));
            fill(0, 0, 0);
            text(String.valueOf(polygonSystem.getAdjacentPolygon(node).size()),
                    (float) drawingPt[0], (float) drawingPt[1] - 10);
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

        // Set up the data again.
        if (keyPressedSet.contains('r')) {
            setup();
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
