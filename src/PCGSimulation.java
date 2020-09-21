import it.unimi.dsi.util.XoShiRo256PlusRandom;
import model.algorithms.geometry.*;
import model.algorithms.geometry.house_generation.HouseGenerationSettings;
import model.algorithms.geometry.house_generation.HouseSizeSettings;
import model.algorithms.geometry.house_generation.HouseType;
import model.algorithms.geometry.tree_generation.TreeFactory;
import model.algorithms.geometry.tree_generation.TreeGenerationSettings;
import model.settings.MapGenerationMode;
import model.surface.Tree;
import model.utils.PhysicUtils;
import view.components.*;
import model.settings.MapGenerationSettings;
import model.terrain.Terrain;
import model.utils.MathUtils;
import model.utils.Triplet;
import processing.core.PApplet;
import processing.event.MouseEvent;
import view.camera.BaseCamera;
import view.camera.CameraConstants;
import view.camera.HexCamera;
import view.constants.DrawingConstants;
import view.drawer.InfoDrawer;
import view.drawer.MapDrawer;
import view.drawer.UIDrawer;
import view.settings.DrawingSettings;

import java.util.*;

import static view.constants.DrawingConstants.ROAD_COLOR;

public class PCGSimulation extends PApplet {

    private final static int INPUT_WIDTH = 1920;
    private final static int INPUT_HEIGHT = 1080;

    private final static int INPUT_TOP_X = 0;
    private final static int INPUT_TOP_Y = 0;
    private final static int INPUT_DIV = 400;
    private final static int INPUT_NUM_X = 50;
    private final static int INPUT_NUM_Y = 50;

    private static int NUM_HEX_RADIUS = 11;
    private static double HEX_RADIUS = 550;
    private static double HEX_JIGGLE = 120;
    private final static double HEX_CENTER_X = INPUT_TOP_X + INPUT_NUM_X * INPUT_DIV / 2;
    private final static double HEX_CENTER_Y = INPUT_TOP_Y + INPUT_NUM_Y * INPUT_DIV / 2;
    private static double HEX_CENTER_JIGGLE = 1800;

    private final static double PT_SCALE = 0.95;
    private final static double SWAP_PROBABILITY = 0.10;

    private final static double LOG_BASE = 20.0;
    private final static double BASE_SCALE_DIST = 7000;

    // Config for the number of nodes
    private static int NUM_VERTICES_INNER_WALL = 10;
    private static int NUM_BLOCKS_OUTER_WALL = 50;

    // Settings
    DrawingSettings drawingSettings;
    MapGenerationSettings mapGenerationSettings;
    HouseGenerationSettings houseGenerationSettings;
    TreeGenerationSettings treeGenerationSettings;

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

    // UI components
    ArrayList<Scrollbar> scrollbars;
    ArrayList<CheckBox> checkBoxes;
    Button resetButton;

    // Terrain
    Terrain terrain;

    // Polygon system
    ArrayList<Vertex> vertexList;
    PolygonFactory polygonFactory;
    TreeFactory treeFactory;
    PolygonHasher polygonHasher;
    PolygonSystem polygonSystem;
    PolygonSystem outerWallSystem;
    int cityGenerationSeed;

    // Important polygons
    Polygon cityCenterPolygon;
    Polygon cityOuterWallPolygon;
    Polygon riverPolygon;
    ArrayList<Polygon> polygonHouses;
    ArrayList<Tree> trees;

    public void settings() {
        size(INPUT_WIDTH, INPUT_HEIGHT, P2D);

        // Drawing settings
        drawingSettings = new DrawingSettings();
        drawingSettings.setShowNumAdjacentPolygons(true);
        drawingSettings.setDrawPolygonEdges(false);
        drawingSettings.setDrawVertices(false);
        drawingSettings.setDrawHouses(true);
        drawingSettings.setDrawRiver(false);
        drawingSettings.setDrawRiverAsCurved(false);
        drawingSettings.setDrawTrees(true);
        drawingSettings.setDrawRoads(true);

        // Map generation settings
        mapGenerationSettings = new MapGenerationSettings();
        mapGenerationSettings.setPointExtension(true);

        // House generation settings
        houseGenerationSettings = new HouseGenerationSettings();
        houseGenerationSettings.setMapGenerationMode(MapGenerationMode.POLYGON_BASED);
        houseGenerationSettings.setDistanceFromEdge(200);
        houseGenerationSettings.setDistanceFromEdgeWiggle(16.0);
        houseGenerationSettings.setDistanceFromOther(32.0);
        houseGenerationSettings.setDistanceFromOtherWiggle(10.0);
        houseGenerationSettings.setDistanceFromCrossRoad(200.0);

        // House size settings
        HashMap<HouseType, HouseSizeSettings> sizeSettingsMap = new HashMap<>();
        HouseSizeSettings regularHouseSettings = new HouseSizeSettings();
        regularHouseSettings.setHouseWidth(160.0);
        regularHouseSettings.setHouseWidthWiggle(50.0);
        regularHouseSettings.setHouseArea(25600.0);
        regularHouseSettings.setHouseAreaWiggle(10000.0);
        sizeSettingsMap.put(HouseType.REGULAR, regularHouseSettings);

        HouseSizeSettings triangleHouseSizeSettings = new HouseSizeSettings();
        triangleHouseSizeSettings.setHouseWidth(220.0);
        triangleHouseSizeSettings.setHouseWidthWiggle(40.0);
        triangleHouseSizeSettings.setHouseArea(51200.0);
        triangleHouseSizeSettings.setHouseAreaWiggle(10000.0);
        sizeSettingsMap.put(HouseType.TRIANGLE, triangleHouseSizeSettings);

        HouseSizeSettings lHouseSizeSettings = new HouseSizeSettings();
        lHouseSizeSettings.setHouseWidth(320.0);
        lHouseSizeSettings.setHouseWidthWiggle(20.0);
        lHouseSizeSettings.setHouseArea(102400);
        lHouseSizeSettings.setHouseAreaWiggle(10000.0);
        sizeSettingsMap.put(HouseType.L, lHouseSizeSettings);

        HouseSizeSettings oHouseSizeSettings = new HouseSizeSettings();
        oHouseSizeSettings.setHouseWidth(640.0);
        oHouseSizeSettings.setHouseWidthWiggle(100.0);
        oHouseSizeSettings.setHouseArea(409600.0);
        oHouseSizeSettings.setHouseAreaWiggle(100000.0);
        sizeSettingsMap.put(HouseType.O, oHouseSizeSettings);
        houseGenerationSettings.setHouseTypeSizeSettings(sizeSettingsMap);

        // House type probabilities
        HashMap<HouseType, Double> houseTypeProbs = new HashMap<>();
        houseTypeProbs.put(HouseType.REGULAR, 0.85);
        houseTypeProbs.put(HouseType.TRIANGLE, 0.05);
        houseTypeProbs.put(HouseType.L, 0.07);
        houseTypeProbs.put(HouseType.O, 0.03);
        houseGenerationSettings.setHouseTypeProbs(houseTypeProbs);
        mapGenerationSettings.setHouseGenerationSettings(houseGenerationSettings);

        // Tree generation settings
        treeGenerationSettings = new TreeGenerationSettings();
        treeGenerationSettings.setDistanceFromEdge(150.0);
        treeGenerationSettings.setDistanceFromEdgeWiggle(40.0);
        treeGenerationSettings.setDistanceFromOther(250.0);
        treeGenerationSettings.setDistanceFromOtherWiggle(150.0);
        treeGenerationSettings.setDistanceFromCrossRoad(150.0);
        treeGenerationSettings.setSize(80);
        treeGenerationSettings.setSizeWiggle(30.0);
        mapGenerationSettings.setTreeGenerationSettings(treeGenerationSettings);

        // Drawing settings
        smooth(3);

        // Some seed at the beginning
        cityGenerationSeed = 42;
    }

    /**
     * Main function to generate terrain based on Configs.
     */
    private void resetContentGeneration() {

        // Reset random generator
        MathUtils.random = new XoShiRo256PlusRandom(cityGenerationSeed);

        // Reset hashers
        polygonHasher = new PolygonHasher(INPUT_DIV, INPUT_DIV);

        // Generate a set of points.
        vertexList = new ArrayList<>();
        HashSet<Triplet<Integer, Integer, Integer>> hexIndices = new HashSet<>();
        for (int i = 0; i < NUM_HEX_RADIUS; i++) {
            for (Triplet triplet : MathUtils.getHexagonalIndicesRingAtOffset(i)) {
                hexIndices.add(triplet);
            }
        }
        int i = 0;
        HashMap<Triplet<Integer, Integer, Integer>, Vertex> vertexMap = new HashMap<>();
        for (Triplet<Integer, Integer, Integer> triplet : hexIndices) {
            // Add the vertex to the vertex list
            double[] pt = MathUtils.generateOffsetBasedOnHexTripletIndices(triplet.x, triplet.y, triplet.z, HEX_RADIUS);
            Vertex vertex = new Vertex(pt[0] + HEX_CENTER_X, pt[1] + HEX_CENTER_Y);
            vertexList.add(vertex);

            // Add the vertex to the vertex map, this will help us identify adjacent vertex later.
            vertexMap.put(triplet, vertex);
            i++;
        }

        // Jiggle the vertices
        for (Vertex vertex : vertexList) {
            double[] newPt = MathUtils.polarJiggle(vertex.getX(), vertex.getY(), HEX_JIGGLE);
            vertex.setX(newPt[0]);
            vertex.setY(newPt[1]);
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
                if (vertexMap.containsKey(t1) && vertexMap.containsKey(t2) && vertexMap.containsKey(t3)) {
                    HashSet<Edge> edges = new HashSet<>();
                    edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t2)));
                    edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t3)));
                    edges.add(new Edge(vertexMap.get(t2), vertexMap.get(t3)));
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
                if (vertexMap.containsKey(t1) && vertexMap.containsKey(t2) && vertexMap.containsKey(t3)) {
                    HashSet<Edge> edges = new HashSet<>();
                    edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t2)));
                    edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t3)));
                    edges.add(new Edge(vertexMap.get(t2), vertexMap.get(t3)));
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
                if (vertexMap.containsKey(t1) && vertexMap.containsKey(t2) && vertexMap.containsKey(t3)) {
                    Polygon triangle;
                    if (vertexMap.containsKey(t1) && vertexMap.containsKey(t2) && vertexMap.containsKey(t3)) {
                        HashSet<Edge> edges = new HashSet<>();
                        edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t2)));
                        edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t3)));
                        edges.add(new Edge(vertexMap.get(t2), vertexMap.get(t3)));
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
                if (vertexMap.containsKey(t1) && vertexMap.containsKey(t2) && vertexMap.containsKey(t3)) {
                    HashSet<Edge> edges = new HashSet<>();
                    edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t2)));
                    edges.add(new Edge(vertexMap.get(t1), vertexMap.get(t3)));
                    edges.add(new Edge(vertexMap.get(t2), vertexMap.get(t3)));
                    triangle = new Polygon(edges);
                    triangles.add(triangle);
                    downTriangleMap.put(curr, triangle);
                }
            }
            numPoints++;
        }

        /**
         * Scale the points further
         */
        // Point extension
        if (mapGenerationSettings.isPointExtension()) {
            for (Vertex v : vertexList) {
                double dist = MathUtils.quickDistance(HEX_CENTER_X, HEX_CENTER_Y, v.getX(), v.getY());
                double[] newPt = MathUtils.scalePoint(
                        new double[]{HEX_CENTER_X, HEX_CENTER_Y},
                        v.getPt(),
                        Math.max(Math.log(dist / BASE_SCALE_DIST) / Math.log(LOG_BASE) + 1, 1));
                v.setX(newPt[0]);
                v.setY(newPt[1]);
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
         * TODO: Create a path from the center, and the smoothen it.
         * TODO: Optionally, we can try to smoothen all path that create land blocks. Path in the old days are curved
         *  anyway.
         * TODO: Need to figure out how to extend a path into an object.
         */

        // A set that keeps tracked of all merged polygons.
        HashSet<Polygon> mergedPolygonSet = new HashSet<>();

        // For the rest of the land, just keep merging any point that has not been parts of a polygon merged.
        double[] innerWallCenter = MathUtils.polarJiggle(HEX_CENTER_X, HEX_CENTER_Y, HEX_CENTER_JIGGLE);
        List<Vertex> vertexList = new ArrayList<>(polygonSystem.getVertices());
        Collections.sort(vertexList, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                double dist1 = MathUtils.quickDistance(o1.getX(), o1.getY(), innerWallCenter[0], innerWallCenter[1]);
                double dist2 = MathUtils.quickDistance(o2.getX(), o2.getY(), innerWallCenter[0], innerWallCenter[1]);
                if (dist1 < dist2) {
                    return -1;
                } else if (dist1 == dist2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (Vertex vertex : vertexList) {
            boolean adjacentNode = false;
            for (Polygon polygon : mergedPolygonSet) {
                if (polygon.getVertices().contains(vertex)) {
                    adjacentNode = true;
                    break;
                }
            }
            if (!adjacentNode) {
                if (polygonSystem.getAdjacentPolygon(vertex) != null) {
                    Polygon mergedPolygon = polygonSystem.mergeMultiplePolygons(
                            new ArrayList<>(polygonSystem.getAdjacentPolygon(vertex)));
                    mergedPolygonSet.add(mergedPolygon);
                }
            }
        }

        // Randomly merge the remaining triangles
        ArrayList<Polygon> remainingPolygons = new ArrayList<>(polygonSystem.getPolygons());
        for (Polygon polygon : remainingPolygons) {
            if (polygon.getVertices().size() == 3) {
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

                // Remove polygon to be merged
                ArrayList<Polygon> mergingPolygons = new ArrayList<>(polygonSystem.getAdjacentPolygon(maxEdge));
                for (Polygon p : mergingPolygons) {
                    mergedPolygonSet.remove(p);
                }
                mergedPolygonSet.add(polygonSystem.mergeMultiplePolygons(mergingPolygons));
            }
        }

        // Make city center.
        vertexList = new ArrayList<>(polygonSystem.getVertices());
        Collections.sort(vertexList, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                double dist1 = MathUtils.quickDistance(o1.getX(), o1.getY(), innerWallCenter[0], innerWallCenter[1]);
                double dist2 = MathUtils.quickDistance(o2.getX(), o2.getY(), innerWallCenter[0], innerWallCenter[1]);
                if (dist1 < dist2) {
                    return -1;
                } else if (dist1 == dist2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        HashSet<Polygon> cityCenterPolygonSet = new HashSet<>();
        for (i = 0; i < NUM_VERTICES_INNER_WALL; i++) {
            for (Polygon polygon : polygonSystem.getAdjacentPolygon(vertexList.get(i))) {
                cityCenterPolygonSet.add(polygon);
            }
        }
        cityCenterPolygon = polygonFactory.createPolygonFromSmallerPolygons(
                cityCenterPolygonSet, EntityType.CITY_CENTER);
        mergedPolygonSet.add(cityCenterPolygon);

        // Merge the remaining blocks to form an outer wall using a different city center.
        double[] outerWallCenter = MathUtils.polarJiggle(innerWallCenter[0], innerWallCenter[1], HEX_CENTER_JIGGLE);
        HashSet<Polygon> remainingBlocks = polygonSystem.getPolygons();
        ArrayList<Polygon> remainingBlocksArr = new ArrayList<>(remainingBlocks);
        Collections.sort(remainingBlocksArr, new Comparator<Polygon>() {
            @Override
            public int compare(Polygon o1, Polygon o2) {
                double[] com1 = o1.getCenterOfMass();
                double[] com2 = o2.getCenterOfMass();
                double dist1 = MathUtils.quickDistance(com1[0], com1[1], outerWallCenter[0], outerWallCenter[1]);
                double dist2 = MathUtils.quickDistance(com2[0], com2[1], outerWallCenter[0], outerWallCenter[1]);
                if (dist1 < dist2) {
                    return -1;
                } else if (dist1 == dist2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        HashSet<Polygon> mergingBlocks = new HashSet<>();
        for (i = 0; i < Math.min(NUM_BLOCKS_OUTER_WALL, remainingBlocksArr.size()); i++) {
            mergingBlocks.add(remainingBlocksArr.get(i));
        }
        outerWallSystem = new PolygonSystem(mergingBlocks);
        cityOuterWallPolygon = polygonFactory.createPolygonFromSmallerPolygons(
                outerWallSystem.getPolygons(), EntityType.OUTER_WALL);
        mergedPolygonSet.add(cityOuterWallPolygon);

        // Make river by connecting to edge polygon together.
        List<Polygon> edgePolygons = polygonSystem.getPolygonsNearTheEdge();
        Polygon riverBegin, riverEnd;
        if (edgePolygons.size() >= 2) {
            riverBegin = edgePolygons.get(0);
            riverEnd = edgePolygons.get(1);
            List<Polygon> riverComponents = polygonSystem.findRiverPathBFS(riverBegin, riverEnd);
            riverPolygon = polygonFactory.createPolygonFromSmallerPolygons(riverComponents, EntityType.RIVER);
            mergedPolygonSet.add(riverPolygon);
        }
        polygonHasher.addObject(riverPolygon);

        // Create houses
        polygonHouses = new ArrayList<>();
        if (mapGenerationSettings.getHouseGenerationSettings().getMapGenerationMode() ==
                MapGenerationMode.EDGE_BASED) {
            for (Edge e : outerWallSystem.getEdges()) {
                ArrayList<Polygon> newHouses = polygonFactory.createHousePolygonsFromEdge(
                        e, polygonHasher);
                for (Polygon p : newHouses) {
                    polygonHouses.add(p);
                }
            }
        } else {
            for (Polygon p : outerWallSystem.getPolygons()) {
                ArrayList<Polygon> newHouses = polygonFactory.createHousePolygonsFromPolygon(
                        p, polygonHasher);
                for (Polygon polygon : newHouses) {
                    polygonHouses.add(polygon);
                }
            }
        }

        // Create trees
        trees = new ArrayList<>();
        for (Edge e : outerWallSystem.getEdges()) {
            ArrayList<Tree> newTrees = treeFactory.generateTreesFromEdge(e, polygonHasher);
            for (Tree tree : newTrees) {
                trees.add(tree);
            }
        }
    }

    public void setup() {
        // Set up terrain.
        terrain = new Terrain(INPUT_TOP_X, INPUT_TOP_Y, INPUT_DIV, INPUT_NUM_X, INPUT_NUM_Y);

        // Setup polygon factory
        polygonFactory = new PolygonFactory(houseGenerationSettings);
        treeFactory = new TreeFactory(treeGenerationSettings);

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
                ((HexCamera) camera).getPhiAngle(), Math.PI / 24, Math.PI * 11 / 24,
                ScrollbarMode.DOUBLE,this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        ((HexCamera) camera).setPhiAngle(value);
                    }
                }));
        scrollbars.add(new AsynchronousScrollbar("Num hex radius",
                INPUT_WIDTH - 300, 90, 280, 20,
                NUM_HEX_RADIUS, 5, 30,
                ScrollbarMode.INTEGER,this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        NUM_HEX_RADIUS = (int) Math.round(value);
                        resetContentGeneration();
                    }
                }));
        scrollbars.add(new AsynchronousScrollbar("Radius of each hex",
                INPUT_WIDTH - 300, 150, 280, 20,
                HEX_RADIUS, 100, 1000,
                ScrollbarMode.DOUBLE,this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        HEX_RADIUS = value;
                        resetContentGeneration();
                    }
                }));
        scrollbars.add(new AsynchronousScrollbar("Hex Jiggle",
                INPUT_WIDTH - 300, 210, 280, 20,
                HEX_JIGGLE, 50, 200,
                ScrollbarMode.DOUBLE,this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        HEX_JIGGLE = value;
                        resetContentGeneration();
                    }
                }));
        scrollbars.add(new AsynchronousScrollbar("Hex Center Jiggle",
                INPUT_WIDTH - 300, 270, 280, 20,
                HEX_CENTER_JIGGLE, 0, 6000,
                ScrollbarMode.DOUBLE,this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        HEX_CENTER_JIGGLE = value;
                        resetContentGeneration();
                    }
                }));
        scrollbars.add(new AsynchronousScrollbar("Num nodes - City Center",
                INPUT_WIDTH - 300, 330, 280, 20,
                NUM_VERTICES_INNER_WALL, 10, 25,
                ScrollbarMode.INTEGER,this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        NUM_VERTICES_INNER_WALL = (int) value;
                        resetContentGeneration();
                    }
                }));
        scrollbars.add(new AsynchronousScrollbar("Num nodes - Outer Wall",
                INPUT_WIDTH - 300, 390, 280, 20,
                NUM_BLOCKS_OUTER_WALL, 15, 200,
                ScrollbarMode.INTEGER,this,
                new CustomAssigner() {
                    @Override
                    public void updateValue(double value) {
                        NUM_BLOCKS_OUTER_WALL = (int) value;
                        resetContentGeneration();
                    }
                }));

        // Reset button
        resetButton = new Button("Reset seed",
                INPUT_WIDTH - 300, 430, 280, 20, this, new CustomProcedure() {
            @Override
            public void proc() {
                cityGenerationSeed = (int) (Integer.MAX_VALUE * (Math.random() - 1) * 2);
                resetContentGeneration();
            }
        });

        // Checkboxes
        checkBoxes = new ArrayList<>();
        checkBoxes.add(new CheckBox("Show polygon edges",
            drawingSettings.isDrawPolygonEdges(),
            INPUT_WIDTH - 300, 470, 280, 25, this,
            new CustomProcedure() {
                @Override
                public void proc() { drawingSettings.setDrawPolygonEdges(true); }
            },
            new CustomProcedure() {
                @Override
                public void proc() { drawingSettings.setDrawPolygonEdges(false);  }
            }));
        checkBoxes.add(new CheckBox("Show vertices",
                drawingSettings.isDrawPolygonEdges(),
                INPUT_WIDTH - 300, 510, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawVertices(true); }
                },
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawVertices(false);  }
                }));
        checkBoxes.add(new CheckBox("Show # adjacent polygons",
                drawingSettings.isShowNumAdjacentPolygons(),
                INPUT_WIDTH - 300, 550, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setShowNumAdjacentPolygons(true); }
                },
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setShowNumAdjacentPolygons(false);  }
                }));
        checkBoxes.add(new CheckBox("Show # adjacent edges",
                drawingSettings.isShowNumAdjacentPolygons(),
                INPUT_WIDTH - 300, 590, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setShowNumAdjacentEdges(true); }
                },
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setShowNumAdjacentEdges(false);  }
                }));
        checkBoxes.add(new CheckBox("Show houses",
                drawingSettings.isDrawHouses(),
                INPUT_WIDTH - 300, 630, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawHouses(true); }
                },
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawHouses(false);  }
                }));
        checkBoxes.add(new CheckBox("Draw river",
                drawingSettings.isDrawRiver(),
                INPUT_WIDTH - 300, 670, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawRiver(true); }
                },
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawRiver(false);  }
                }));
        checkBoxes.add(new CheckBox("Draw trees",
                drawingSettings.isDrawTrees(),
                INPUT_WIDTH - 300, 710, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawTrees(true); }
                },
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawTrees(false);  }
                }));
        checkBoxes.add(new CheckBox("Draw roads",
                drawingSettings.isDrawRoads(),
                INPUT_WIDTH - 300, 750, 280, 25, this,
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawRoads(true); }
                },
                new CustomProcedure() {
                    @Override
                    public void proc() { drawingSettings.setDrawRoads(false);  }
                }));

        // Set up drawer
        uiDrawer = new UIDrawer(this, camera, drawingSettings);
        mapDrawer = new MapDrawer(this, camera);
        infoDrawer = new InfoDrawer(this);

        // Set of keys pressed.
        keyPressedSet = new HashSet<>();

        // Create content
        resetContentGeneration();
    }

    public void draw() {

        // Perform some backend update before drawing
        for (Scrollbar scrollbar : scrollbars) {
            scrollbar.update();
        }
        resetButton.update();
        for (CheckBox checkBox : checkBoxes) {
            checkBox.update();
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
        int[] color = DrawingConstants.MOUSE_OVER_POLYGON_COLOR;

        // Draw polygon
        // We will first draw all the polygon with no stroke, and the draw the polygon edges. This is to avoid drawing
        // the same edge twice.
        noStroke();
        for (Polygon polygon : polygonSystem.getPolygons()) {
            if (polygon.getEntityType() == EntityType.RIVER) continue;
            double[][] boundaryPts = polygon.getBoundaryPoints();

            // Check if the polygon should be drawn. The polygon should be drawn if one of the point is visible to
            // the camera
            boolean visible = false;
            for (double[] pt : boundaryPts) {
                if (camera.positionIsVisible(pt[0], pt[1])) {
                    visible = true;
                    break;
                }
            }
            if (!visible) continue;

            // Determine the color of the polygon
            double[] mousePosition = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
            if (PhysicUtils.checkPolygonPointCollision(boundaryPts, mousePosition[0], mousePosition[1])) {
                fill(color[0],color[1],color[2],50);
            } else {
                fill(color[0],color[1],color[2],23);
            }
            beginShape();
            for (int i = 0; i < boundaryPts.length; i++) {
                double x = boundaryPts[i][0];
                double y = boundaryPts[i][1];
                double[] drawingPt = camera.getDrawingPosition(x, y, terrain.getHeightFromPos(x, y));
                vertex((float) drawingPt[0], (float) drawingPt[1]);
            }
            endShape(CLOSE);
        }
        if (drawingSettings.isDrawPolygonEdges()) {
            stroke(color[0], color[1], color[2], 100);
            strokeWeight(1);
            beginShape(LINES);
            for (Edge edge : polygonSystem.getEdges()) {
                double x1 = edge.getVertex1().getX();
                double y1 = edge.getVertex1().getY();
                double x2 = edge.getVertex2().getX();
                double y2 = edge.getVertex2().getY();
                if (!camera.positionIsVisible(x1, y1) && !camera.positionIsVisible(x2, y2)) continue;

                // Determine the color of the polygon
                double[] drawingPt1 = camera.getDrawingPosition(x1, y1, terrain.getHeightFromPos(x1, y1));
                double[] drawingPt2 = camera.getDrawingPosition(x2, y2, terrain.getHeightFromPos(x2, y2));
                vertex((float) drawingPt1[0], (float) drawingPt1[1]);
                vertex((float) drawingPt2[0], (float) drawingPt2[1]);
            }
            endShape(CLOSE);
        }

        // Draw roads
        if (drawingSettings.isDrawRoads()) {
            color = ROAD_COLOR;
            stroke(color[0], color[1], color[2], 100);
            strokeWeight(3);
            beginShape(LINES);
            for (Edge e : outerWallSystem.getEdges()) {
                if ((riverPolygon.getVertices().contains(e.getVertex1()) &&
                        riverPolygon.getVertices().contains(e.getVertex2())) ||
                        PhysicUtils.checkPolygonPointCollision(
                                riverPolygon.getBoundaryPoints(),
                                (e.getVertex1().getX() + e.getVertex2().getX()) / 2,
                                (e.getVertex2().getY() + e.getVertex2().getY()) / 2)) {
                    continue;
                }
                double[] drawingPt1 = camera.getDrawingPosition(e.getVertex1().getX(), e.getVertex1().getY(),
                        terrain.getHeightFromPos(e.getVertex1().getX(), e.getVertex1().getY()));
                double[] drawingPt2 = camera.getDrawingPosition(e.getVertex2().getX(), e.getVertex2().getY(),
                        terrain.getHeightFromPos(e.getVertex2().getX(), e.getVertex2().getY()));
                vertex((float) drawingPt1[0], (float) drawingPt1[1]);
                vertex((float) drawingPt2[0], (float) drawingPt2[1]);
            }
            endShape();
        }

        // Draw river polygon
        double[][] boundaryPts;
        if (drawingSettings.isDrawRiver()) {
            color = DrawingConstants.POLYGON_RIVER_COLOR;
            boundaryPts = riverPolygon.getBoundaryPoints();
            if (camera.boundaryPointsAreVisible(boundaryPts)) {
                // Determine the color of the polygon
                double[] mousePosition = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                if (PhysicUtils.checkPolygonPointCollision(boundaryPts, mousePosition[0], mousePosition[1])) {
                    stroke(color[0],color[1],color[2],150);
                    strokeWeight(4);
                } else {
                    stroke(color[0],color[1],color[2],100);
                    strokeWeight(4);
                }
                fill(color[0],color[1],color[2],128);
                beginShape();
                for (int i = 0; i < boundaryPts.length; i++) {
                    double x = boundaryPts[i][0];
                    double y = boundaryPts[i][1];
                    double[] drawingPt = camera.getDrawingPosition(x, y, terrain.getHeightFromPos(x, y));
                    if (drawingSettings.isDrawRiverAsCurved()) {
                        curveVertex((float) drawingPt[0], (float) drawingPt[1]);
                    } else {
                        vertex((float) drawingPt[0], (float) drawingPt[1]);
                    }
                }
                endShape(CLOSE);
            }
        }

        // Draw city center polygon
        noFill();
        boundaryPts = cityCenterPolygon.getBoundaryPoints();
        if (camera.boundaryPointsAreVisible(boundaryPts)) {
            // Determine the color of the polygon
            double[] mousePosition = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
            boolean mouseOver = PhysicUtils.checkPolygonPointCollision(boundaryPts, mousePosition[0], mousePosition[1]);
            if (PhysicUtils.checkPolygonPointCollision(boundaryPts, mousePosition[0], mousePosition[1])) {
                color = DrawingConstants.MOUSE_OVER_POLYGON_COLOR;
                stroke(color[0],color[1],color[2]);
                strokeWeight(4);
            } else {
                color = DrawingConstants.NORMAL_POLYGON_COLOR;
                stroke(color[0],color[1],color[2]);
                strokeWeight(4);
            }
            beginShape(LINES);
            for (Edge e : cityCenterPolygon.getEdges()) {
                // TODO: This essentially tries to check if the wall overlaps the river. However, floating point
                //  inaccuracy in the system causes this check to be flawed. This check also looks really wordy.
                //  Improve when time permits.
                if ((riverPolygon.getVertices().contains(e.getVertex1()) &&
                        riverPolygon.getVertices().contains(e.getVertex2())) ||
                        PhysicUtils.checkPolygonPointCollision(
                                riverPolygon.getBoundaryPoints(),
                                (e.getVertex1().getX() + e.getVertex2().getX()) / 2,
                                (e.getVertex2().getY() + e.getVertex2().getY()) / 2)) {
                    continue;
                }
                double[] drawingPt1 = camera.getDrawingPosition(e.getVertex1().getX(), e.getVertex1().getY(),
                        terrain.getHeightFromPos(e.getVertex1().getX(), e.getVertex1().getY()));
                double[] drawingPt2 = camera.getDrawingPosition(e.getVertex2().getX(), e.getVertex2().getY(),
                        terrain.getHeightFromPos(e.getVertex2().getX(), e.getVertex2().getY()));
                vertex((float) drawingPt1[0], (float) drawingPt1[1]);
                vertex((float) drawingPt2[0], (float) drawingPt2[1]);
            }
            endShape();
            if (mouseOver) {
                fill(0, 0, 0);
                rect(mouseX, mouseY, 100, 30);
                textAlign(CENTER, CENTER);
                fill(255, 255, 255);
                text(cityCenterPolygon.getEntityType().toString(), mouseX + 50, mouseY + 15);
            }
        }

        // Draw city outer wall polygon
        boundaryPts = cityOuterWallPolygon.getBoundaryPoints();
        noFill();
        if (camera.boundaryPointsAreVisible(boundaryPts)) {
            // Determine the color of the polygon
            double[] mousePosition = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
            if (PhysicUtils.checkPolygonPointCollision(boundaryPts, mousePosition[0], mousePosition[1])) {
                color = DrawingConstants.MOUSE_OVER_POLYGON_COLOR;
                stroke(color[0],color[1],color[2]);
                strokeWeight(4);
            } else {
                color = DrawingConstants.NORMAL_POLYGON_COLOR;
                stroke(color[0],color[1],color[2]);
                strokeWeight(4);
            }
            beginShape(LINES);
            for (Edge e : cityOuterWallPolygon.getEdges()) {
                if ((riverPolygon.getVertices().contains(e.getVertex1()) &&
                    riverPolygon.getVertices().contains(e.getVertex2())) ||
                    PhysicUtils.checkPolygonPointCollision(
                            riverPolygon.getBoundaryPoints(),
                            (e.getVertex1().getX() + e.getVertex2().getX()) / 2,
                            (e.getVertex2().getY() + e.getVertex2().getY()) / 2)) {
                    continue;
                }
                double[] drawingPt1 = camera.getDrawingPosition(e.getVertex1().getX(), e.getVertex1().getY(),
                        terrain.getHeightFromPos(e.getVertex1().getX(), e.getVertex1().getY()));
                double[] drawingPt2 = camera.getDrawingPosition(e.getVertex2().getX(), e.getVertex2().getY(),
                        terrain.getHeightFromPos(e.getVertex2().getX(), e.getVertex2().getY()));
                vertex((float) drawingPt1[0], (float) drawingPt1[1]);
                vertex((float) drawingPt2[0], (float) drawingPt2[1]);
            }
            endShape();
        }

        // Draw houses
        noStroke();
        if (drawingSettings.isDrawHouses()) {
            color = DrawingConstants.MOUSE_OVER_POLYGON_COLOR;
            for (Polygon polygon : polygonHouses) {
                boundaryPts = polygon.getBoundaryPoints();
                if (camera.boundaryPointsAreVisible(boundaryPts)) {
                    // Determine the color of the polygon
                    double[] mousePosition = camera.getActualPositionFromScreenPosition(mouseX, mouseY);
                    if (PhysicUtils.checkPolygonPointCollision(boundaryPts, mousePosition[0], mousePosition[1])) {
                        fill(color[0],color[1],color[2],60);
                    } else {
                        fill(color[0],color[1],color[2],35);
                    }
                    beginShape();
                    for (int i = 0; i < boundaryPts.length; i++) {
                        double x = boundaryPts[i][0];
                        double y = boundaryPts[i][1];
                        double[] drawingPt = camera.getDrawingPosition(x, y, terrain.getHeightFromPos(x, y));
                        vertex((float) drawingPt[0], (float) drawingPt[1]);
                    }
                    endShape(CLOSE);
                }
            }
        }

        // Draw trees
        if (drawingSettings.isDrawTrees()) {
            color = DrawingConstants.TREE_LEAF_COLOR;
            for (Tree tree : trees) {
                noStroke();
                fill(color[0], color[1], color[2], 200);
                if (!camera.positionIsVisible(tree.getX(), tree.getY())) continue;
                double[] drawingPt = camera.getDrawingPosition(
                        tree.getX(), tree.getY(), terrain.getHeightFromPos(tree.getX(), tree.getY()));
                ellipse((float) drawingPt[0], (float) drawingPt[1],
                        (float) (tree.getRadius() * camera.getZoom()),
                        (float) (tree.getRadius() * camera.getZoom() *
                                MathUtils.quickCos((float) ((HexCamera) camera).getPhiAngle())));
            }
        }

        // Draw points
        if (drawingSettings.isDrawVertices()) {
            color = DrawingConstants.NODE_COLOR;
            for (Vertex vertex : polygonSystem.getVertices()) {
                noStroke();
                fill(color[0], color[1], color[2], color[3]);
                if (!camera.positionIsVisible(vertex.getX(), vertex.getY())) continue;
                double[] drawingPt = camera.getDrawingPosition(
                        vertex.getX(), vertex.getY(), terrain.getHeightFromPos(vertex.getX(), vertex.getY()));
                circle((float) drawingPt[0], (float) drawingPt[1], (float) (DrawingConstants.NODE_RADIUS * camera.getZoom()));
                if (drawingSettings.isShowNumAdjacentPolygons()) {
                    fill(0, 0, 0);
                    textAlign(LEFT, BOTTOM);
                    if (polygonSystem.getAdjacentPolygon(vertex) == null) {
                        text("0", (float) drawingPt[0], (float) drawingPt[1] - 10);
                    } else {
                        text(String.valueOf(polygonSystem.getAdjacentPolygon(vertex).size()),
                                (float) drawingPt[0], (float) drawingPt[1] - 10);
                    }
                }
                if (drawingSettings.isShowNumAdjacentEdges()) {
                    fill(0, 0, 0);
                    textAlign(LEFT, BOTTOM);
                    text(String.valueOf(polygonSystem.getAdjacentEdges(vertex).size()),
                            (float) drawingPt[0], (float) drawingPt[1] - 10);
                }
            }
        }

        // Draw zoom information
        StringBuilder s = new StringBuilder();
        s.append("Camera shake level              : " + String.format("%.2f", camera.getCameraShakeLevel()) + "\n");
        s.append("Zoom level                      : " + String.format("%.2f", camera.getZoom()) + "\n");
        infoDrawer.drawTextBox(s.toString(), 5, INPUT_HEIGHT - 5, 400);

        // UI components
        for (Scrollbar scrollbar : scrollbars) {
            scrollbar.display();
        }
        resetButton.display();
        for (CheckBox checkBox : checkBoxes) {
            checkBox.display();
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
