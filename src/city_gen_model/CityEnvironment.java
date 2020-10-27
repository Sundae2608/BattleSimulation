package city_gen_model;

import city_gen_model.algorithms.geometry.*;
import city_gen_model.algorithms.geometry.tree_generation.TreeFactory;
import city_gen_model.city_events.MapEventBroadcaster;
import it.unimi.dsi.util.XoShiRo256PlusRandom;
import model.map_objects.House;
import model.map_objects.Tree;
import model.settings.MapGenerationMode;
import model.settings.MapGenerationSettings;
import model.terrain.Terrain;
import model.utils.MathUtils;
import model.utils.Triplet;

import java.util.*;

public class CityEnvironment {

    // City generation constants
    private final static int INPUT_TOP_X = 0;
    private final static int INPUT_TOP_Y = 0;
    private final static int INPUT_DIV = 400;
    private final static int INPUT_NUM_X = 50;
    private final static int INPUT_NUM_Y = 50;
    private final static int NUM_HEX_RADIUS = 9;
    private final static double HEX_RADIUS = 1000;

    private final static double HEX_JIGGLE = 120;
    private final static double HEX_CENTER_X = INPUT_TOP_X + INPUT_NUM_X * INPUT_DIV / 2;
    private final static double HEX_CENTER_Y = INPUT_TOP_Y + INPUT_NUM_Y * INPUT_DIV / 2;
    private final double HEX_CENTER_JIGGLE = 1800;


    private final static double SWAP_PROBABILITY = 0.10;
    private final static double LOG_BASE = 20.0;
    private final static double BASE_SCALE_DIST = 7000;

    // Config for the number of nodes
    private static int NUM_VERTICES_INNER_WALL = 16;
    private static int NUM_BLOCKS_OUTER_WALL = 50;

    // Settings
    MapGenerationSettings mapGenerationSettings;

    // Terrain
    Terrain terrain;

    // City variables
    ArrayList<Tree> theoreticalTree;
    ArrayList<House> theoreticalHouses;
    PriorityQueue<House> housePriorityQueue;
    HashSet<Tree> trees;
    HashSet<House> houses;
    CityState cityState;
    MapEventBroadcaster broadcaster;

    // Random number that generates the city
    int cityGenerationSeed;

    public CityEnvironment(CityState cityState,
                           Terrain terrain,
                           MapEventBroadcaster eventBroadcaster,
                           MapGenerationSettings mapGenerationSettings) {
        this.cityState = cityState;
        this.terrain = terrain;
        this.mapGenerationSettings = mapGenerationSettings;
        this.broadcaster = eventBroadcaster;
        reset();
    }

    /**
     * Step function. Move one time step and update the city states.
     */
    public void step() {
        // TODO: Put a city parameters called "deltaParams" in the city state. This is a fairly ugly way to calculate
        //  difference in the number of houses.
        int oldNumHouses = cityState.getCityStateParameters().getQuantity(CityParamType.HOUSE);
        cityState.update();
        int deltaNumHouses = cityState.getCityStateParameters().getQuantity(CityParamType.HOUSE) - oldNumHouses;
        // TODO: Divide by 4 is somewhat ad hoc. Remove this.
        int newHouses = deltaNumHouses;
        if (newHouses > 0) {
            // If the number of new houses is positive, we need to build a few new houses.
            for (int i = 0; i < newHouses; i++) {
                houses.add(housePriorityQueue.poll());
            }
        } else {
            // If the number of new houses are negative, we need to randomly remove houses.
            int removeHouses = -newHouses;
            ArrayList<House> currentHouses = new ArrayList<>(houses);
            int numHouses = currentHouses.size();
            for (int i = 0; i < numHouses && i < removeHouses; i++) {
                houses.remove(currentHouses.get(i));
                housePriorityQueue.add(currentHouses.get(i));
            }
        }
    }

    /**
     * Main function to generate city. This function will reset all the city content. Population will drop to zero
     */
    public void reset() {

        // Reset random generator
        MathUtils.random = new XoShiRo256PlusRandom(cityGenerationSeed);

        // Initiate local variables. These include geometric containers to help store some useful temporary hasher.
        ArrayList<Vertex> vertexList;
        PolygonHasher polygonHasher;
        PolygonSystem polygonSystem;
        PolygonSystem outerWallSystem;
        Polygon riverPolygon;
        Polygon cityCenterPolygon;
        Polygon cityOuterWallPolygon;
        ArrayList<Polygon> housePolygons;

        // Factories to create new polygons and tree
        PolygonFactory polygonFactory = new PolygonFactory(mapGenerationSettings.getHouseGenerationSettings());
        TreeFactory treeFactory = new TreeFactory(mapGenerationSettings.getTreeGenerationSettings());

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
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x - 1, curr.y, curr.z + 1);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y - 1, curr.z + 1);
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
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x + 1, curr.y - 1, curr.z);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y - 1, curr.z + 1);
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
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x - 1, curr.y, curr.z + 1);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y - 1, curr.z + 1);
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
                Triplet<Integer, Integer, Integer> t2 = new Triplet<>(curr.x + 1, curr.y - 1, curr.z);
                Triplet<Integer, Integer, Integer> t3 = new Triplet<>(curr.x, curr.y - 1, curr.z + 1);
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
            List<Polygon> riverComponents = PolygonUtils.findRiverPathBFS(polygonSystem, riverBegin, riverEnd);
            riverPolygon = polygonFactory.createPolygonFromSmallerPolygons(riverComponents, EntityType.RIVER);
            mergedPolygonSet.add(riverPolygon);
            polygonHasher.addObject(riverPolygon);
        }

        // Randomly divide some of the in the inner city
        remainingPolygons = new ArrayList<>(outerWallSystem.getPolygons());
        for (Polygon p : remainingPolygons) {
            if (!cityCenterPolygonSet.contains(p)) continue;
            double rand = MathUtils.randUniform();
            if (mapGenerationSettings.getDividePolygonProb() > rand) {
                ArrayList<Polygon> newlyCutPolygons = PolygonUtils.dividePolygonsUsingVerticalStrip(
                        p, 2, 4);
                outerWallSystem.removePolygon(p);
                for (Polygon polygon : newlyCutPolygons) {
                    outerWallSystem.addPolygon(polygon);
                }
            }
        }

        // Create houses
        housePolygons = new ArrayList<>();
        if (mapGenerationSettings.getHouseGenerationSettings().getMapGenerationMode() ==
                MapGenerationMode.EDGE_BASED) {
            for (Edge e : outerWallSystem.getEdges()) {
                ArrayList<Polygon> newHouses = polygonFactory.createHousePolygonsFromEdge(
                        e, polygonHasher);
                for (Polygon p : newHouses) {
                    housePolygons.add(p);
                }
            }
        } else {
            for (Polygon p : outerWallSystem.getPolygons()) {
                ArrayList<Polygon> newHouses = polygonFactory.createHousePolygonsFromPolygon(
                        p, polygonHasher);
                for (Polygon polygon : newHouses) {
                    housePolygons.add(polygon);
                }
            }
        }

        // Create trees and houses
        theoreticalTree = new ArrayList<>();
        for (Edge e : outerWallSystem.getEdges()) {
            ArrayList<Tree> newTrees = treeFactory.generateTreesFromEdge(e, polygonHasher);
            for (Tree tree : newTrees) {
                theoreticalTree.add(tree);
            }
        }
        theoreticalHouses = new ArrayList<>();
        housePriorityQueue = new PriorityQueue<>(new Comparator<House>() {
            @Override
            public int compare(House o1, House o2) {
                double dist1 = MathUtils.distance(o1.getX(), o1.getY(), HEX_CENTER_X, HEX_CENTER_Y);
                double dist2 = MathUtils.distance(o2.getX(), o2.getY(), HEX_CENTER_X, HEX_CENTER_Y);
                if (dist1 - dist2 < 0) {
                    return -1;
                } else if (dist1 == dist2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (Polygon p : housePolygons) {
            House h = new House(p.getBoundaryPoints());
            theoreticalHouses.add(h);
            housePriorityQueue.add(h);
        }

        // Reset city state
        trees = new HashSet<>();
        houses = new HashSet<>();
    }

    public HashSet<Tree> getTrees() {
        return trees;
    }

    public HashSet<House> getHouses() {
        return houses;
    }
}
