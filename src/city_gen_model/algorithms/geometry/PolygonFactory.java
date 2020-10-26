package city_gen_model.algorithms.geometry;

import city_gen_model.algorithms.geometry.house_generation.HouseDirection;
import city_gen_model.algorithms.geometry.house_generation.HouseGenerationSettings;
import city_gen_model.algorithms.geometry.house_generation.HouseSizeSettings;
import city_gen_model.algorithms.geometry.house_generation.HouseType;
import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PolygonFactory {

    // Internal storage of settings
    HouseGenerationSettings houseGenSettings;

    // Singleton variables for house type probabilities. Help save time when selecting house randomly from probability.
    private HashMap<HouseType, Double> cumulativeMaps;
    private HouseType[] houseTypeKeys;
    private double sumProb;

    public PolygonFactory(HouseGenerationSettings inputHouseGenerationSettings) {
        houseGenSettings = inputHouseGenerationSettings;

        // Process house type probability singletons, which are later used to randomly select house type.
        HashMap<HouseType, Double> probMap = houseGenSettings.getHouseTypeProb();
        houseTypeKeys = new HouseType[probMap.size()];
        int i = 0;
        for (HouseType key : probMap.keySet()) {
            houseTypeKeys[i] = key;
            i++;
        }
        cumulativeMaps = new HashMap<>();
        double currProb = 0;
        for (HouseType key : houseTypeKeys) {
            currProb += probMap.get(key);
            cumulativeMaps.put(key, currProb);
        }
        sumProb = currProb;
    }
    /**
     * Create a bigger polygon with certain type by merging from input of smaller polygon.
     * @param inputPolygons
     * @param type
     * @return
     */
    public Polygon createPolygonFromSmallerPolygons(HashSet<Polygon> inputPolygons, EntityType type) {
        PolygonSystem newSystem = new PolygonSystem(inputPolygons);
        Polygon newPolygon = newSystem.mergeMultiplePolygons(inputPolygons);
        newPolygon.setEntityType(type);
        return newPolygon;
    }
    public Polygon createPolygonFromSmallerPolygons(List<Polygon> inputPolygons, EntityType type) {
        HashSet<Polygon> inputPolygonSet = new HashSet<>(inputPolygons);
        return createPolygonFromSmallerPolygons(inputPolygonSet, type);
    }

    /**
     * Pick a random house type based on probability given by HouseGenerationSettings
     */
    private HouseType randHouseType() {

        // Pick one random house type based on given probability
        double rand = MathUtils.randDouble(0, sumProb);
        for (HouseType key : houseTypeKeys) {
            if (rand < cumulativeMaps.get(key)) {
                return key;
            }
        }
        return houseTypeKeys[houseTypeKeys.length- 1];
    }

    /**
     * Generate a polygon given a set of ratio pts.
     * The new polygon will have the top left of house at (topLeftX, topLeftY), facing angle and has the size of
     * (width, length)
     */
    private Polygon createPolygonBasedOnRatioPts(double topLeftX, double topLeftY, double angle,
                                                 double width, double length,
                                                 double[][] ratioPts) {
        // Calculate unit vectors
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));

        // Create vertices based on given pts.
        ArrayList<Vertex> verticesArr = new ArrayList<>();
        for (int i = 0; i < ratioPts.length; i++) {
            verticesArr.add(new Vertex(
                    topLeftX + ratioPts[i][0] * sideUnitX * width + ratioPts[i][1] * downUnitX * length,
                    topLeftY + ratioPts[i][0] * sideUnitY * width + ratioPts[i][1] * downUnitY * length
            ));
        }
        HashSet<Vertex> vertices = new HashSet<>(verticesArr);

        // Create edges based on given vertices.
        HashSet<Edge> edges = new HashSet<>();
        for (int i = 0; i < verticesArr.size(); i++) {
            Vertex v1 = verticesArr.get(i);
            Vertex v2 = verticesArr.get((i + 1) % verticesArr.size());
            edges.add(new Edge(v1, v2));
        }

        // Create polygon based on given edges and vertices
        return new Polygon(vertices, edges);
    }

    /**
     * @param topLeftX x-position of the top-left corner of the build
     * @param topLeftY y-position of the top-left corner of the build
     * @param angle the angle that the front of the house will be facing
     * @return
     */
    private Polygon generateHouse(
            double topLeftX, double topLeftY, double angle, HouseGenerationSettings houseGenSettings) {
        HouseType type = randHouseType();
        HouseSizeSettings houseSizeSettings = houseGenSettings.getHouseTypeSizeSettings().get(type);

        // Get width and height
        double width = houseSizeSettings.getHouseWidth() +
                MathUtils.randDouble(-houseSizeSettings.getHouseWidthWiggle(), houseSizeSettings.getHouseWidthWiggle());
        double area = houseSizeSettings.getHouseArea() +
                MathUtils.randDouble(-houseSizeSettings.getHouseAreaWiggle(), houseSizeSettings.getHouseAreaWiggle());
        double length = area / width;

        // Build the house
        Polygon p;
        switch (type) {
            case TRIANGLE:
                p = generateTriangleHouse(topLeftX, topLeftY, angle, width, length);
                break;
            case L:
                p = generateLHouse(topLeftX, topLeftY, angle, width, length);
                break;
            case O:
                p = generateOHouse(topLeftX, topLeftY, angle, width, length);
                break;
            case U:
                p = generateUHouse(topLeftX, topLeftY, angle, width, length);
                break;
            case H:
                p = generateHHouse(topLeftX, topLeftY, angle, width, length);
                break;
            case T:
                p = generateTHouse(topLeftX, topLeftY, angle, width, length);
                break;
            case PLUS:
                p = generatePlusHouse(topLeftX, topLeftY, angle, width, length);
                break;
            case REGULAR:
            default:
                p = generateRectangleHouse(topLeftX, topLeftY, angle, width, length);
                break;
        }
        p.setEntityType(EntityType.HOUSE);
        return p;
    }

    /**
     * Create house polygons that are inside of the main Polygon.
     */
    public ArrayList<Polygon> createHousePolygonsFromPolygon(
            Polygon mainPolygon, PolygonHasher hasher) {
        // Go through each edge of the polygon in clockwise order, and generate all the houses on the left side.
        ArrayList<Polygon> returnPolygons = new ArrayList<>();
        ArrayList<Vertex> verticesList = mainPolygon.getOrderedVertices();
        int numPts = verticesList.size();
        for (int i = 0; i < numPts; i++) {
            Vertex v1 = verticesList.get(i);
            Vertex v2 = verticesList.get((i + 1) % numPts);
            double roadDistance = MathUtils.quickDistance(v1.x, v1.y, v2.x, v2.y);
            double angle = MathUtils.atan2(v2.y - v1.y, v2.x - v1.x);
            double beginX = v1.x;
            double beginY = v1.y;
            double roadUnitX = MathUtils.quickCos((float) angle);
            double roadUnitY = MathUtils.quickSin((float) angle);
            double rightSideUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
            double rightSideUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));

            // Generate the left-side houses
            double currentDist = houseGenSettings.getDistanceFromCrossRoad();
            Polygon lastPolygon = null;
            while (true) {
                // Generate a house if possible
                double distFromEdge = houseGenSettings.getDistanceFromEdge() +
                        MathUtils.randDouble(-houseGenSettings.getDistanceFromEdgeWiggle(), houseGenSettings.getDistanceFromEdgeWiggle());
                double topleftX = beginX + roadUnitX * currentDist + distFromEdge * rightSideUnitX;
                double topLeftY = beginY + roadUnitY * currentDist + distFromEdge * rightSideUnitY;
                if (lastPolygon != null &&
                        PhysicUtils.checkPolygonPointCollision(lastPolygon.getBoundaryPoints(), topleftX, topLeftY)) {
                    // TODO: Be careful! This code will fail if distanceFromOther() == 0. Implement a check to ensure
                    //  that will not happen.
                    double distFromHouse = houseGenSettings.getDistanceFromOther() +
                            MathUtils.randDouble(
                                    -houseGenSettings.getDistanceFromOtherWiggle(),
                                    houseGenSettings.getDistanceFromOtherWiggle());
                    currentDist = currentDist + distFromHouse;
                    continue;
                }
                Polygon newPolygon = generateHouse(
                        topleftX, topLeftY, angle, houseGenSettings);

                // Check the newly generated house with the hasher and make sure that it does not collide with existing
                // house
                boolean collide = false;
                for (Polygon p : hasher.getCollisionObjects(newPolygon)) {
                    if (PhysicUtils.checkPolygonPolygonCollision(
                            p.getBoundaryPoints(), newPolygon.getBoundaryPoints())) {
                        collide = true;
                        break;
                    };
                }

                // Check newly generated house to make sure that they are fully inside the polygon
                boolean inside = PhysicUtils.checkPolygonContainsPolygon(
                        mainPolygon.getBoundaryPoints(), newPolygon.getBoundaryPoints());
                if (!collide && inside) {
                    returnPolygons.add(newPolygon);
                    hasher.addObject(newPolygon);
                    lastPolygon = newPolygon;
                }
                double distFromHouse = houseGenSettings.getDistanceFromOther() +
                        MathUtils.randDouble(-houseGenSettings.getDistanceFromOtherWiggle(), houseGenSettings.getDistanceFromOtherWiggle());
                currentDist = currentDist + distFromHouse;
                if (currentDist > roadDistance - houseGenSettings.getDistanceFromCrossRoad()) break;
            }
        }
        return returnPolygons;
    }

    /**
     * Create a list of rectangular polygons representing houses along the edge, which supposedly represent the streets.
     * @param e The edge in question
     * @param hasher A hasher which hashed all polygons to ensure generated polygons do not overlap.
     * @return The list of polygons, each of which representing a house.
     */
    public ArrayList<Polygon> createHousePolygonsFromEdge(
            Edge e, PolygonHasher hasher) {
        // Pre-calculate  several directional information
        Vertex vertex1 = e.getVertex1();
        Vertex vertex2 = e.getVertex2();
        double roadDistance = MathUtils.quickDistance(vertex1.x, vertex1.y, vertex2.x, vertex2.y);
        double angle = MathUtils.atan2(vertex2.y - vertex1.y, vertex2.x - vertex1.x);
        double beginX = vertex1.x;
        double beginY = vertex1.y;
        double roadUnitX = MathUtils.quickCos((float) angle);
        double roadUnitY = MathUtils.quickSin((float) angle);
        double rightSideUnitX = MathUtils.quickCos((float) (angle - MathUtils.PIO2));
        double rightSideUnitY = MathUtils.quickSin((float) (angle - MathUtils.PIO2));
        double leftSideUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double leftSideUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));

        // Generate the left-side houses
        ArrayList<Polygon> polygons = new ArrayList<>();
        double currentDist = houseGenSettings.getDistanceFromCrossRoad();
        Polygon lastPolygon = null;
        while (true) {
            // Generate a house if possible
            double distFromEdge = houseGenSettings.getDistanceFromEdge() +
                    MathUtils.randDouble(-houseGenSettings.getDistanceFromEdgeWiggle(), houseGenSettings.getDistanceFromEdgeWiggle());
            double topleftX = beginX + roadUnitX * currentDist + distFromEdge * leftSideUnitX;
            double topLeftY = beginY + roadUnitY * currentDist + distFromEdge * leftSideUnitY;
            if (lastPolygon != null && PhysicUtils.checkPolygonPointCollision(lastPolygon.getBoundaryPoints(), topleftX, topLeftY)) {
                double distFromHouse = houseGenSettings.getDistanceFromOther() +
                        MathUtils.randDouble(-houseGenSettings.getDistanceFromOtherWiggle(), houseGenSettings.getDistanceFromOtherWiggle());
                currentDist = currentDist + distFromHouse;
                continue;
            }
            Polygon newPolygon = generateHouse(
                    topleftX, topLeftY, angle, houseGenSettings);

            // Check the newly generated house with the hasher and make sure that it does not collide with existing
            // house
            boolean collide = false;
            for (Polygon p : hasher.getCollisionObjects(newPolygon)) {
                if (PhysicUtils.checkPolygonPolygonCollision(
                        p.getBoundaryPoints(), newPolygon.getBoundaryPoints())) {
                    collide = true;
                    break;
                };
            }
            if (!collide) {
                polygons.add(newPolygon);
                hasher.addObject(newPolygon);
                lastPolygon = newPolygon;
            }
            double distFromHouse = houseGenSettings.getDistanceFromOther() +
                    MathUtils.randDouble(
                            -houseGenSettings.getDistanceFromOtherWiggle(), houseGenSettings.getDistanceFromEdgeWiggle());
            currentDist = currentDist + distFromHouse;
            if (currentDist > roadDistance -  houseGenSettings.getDistanceFromCrossRoad()) break;
        }

        // Generate the right-side houses
        currentDist = houseGenSettings.getDistanceFromCrossRoad();
        beginX = vertex2.x;
        beginY = vertex2.y;
        angle = angle + Math.PI;
        lastPolygon = null;
        while (true) {
            // Generate a house if possible
            double distFromEdge = houseGenSettings.getDistanceFromEdge() +
                    MathUtils.randDouble(-houseGenSettings.getDistanceFromEdgeWiggle(), houseGenSettings.getDistanceFromEdgeWiggle());
            double topleftX = beginX + roadUnitX * currentDist + distFromEdge * rightSideUnitX;
            double topLeftY = beginY + roadUnitY * currentDist + distFromEdge * rightSideUnitY;
            if (lastPolygon != null && PhysicUtils.checkPolygonPointCollision(lastPolygon.getBoundaryPoints(), topleftX, topLeftY)) {
                double distFromHouse = houseGenSettings.getDistanceFromOther() +
                        MathUtils.randDouble(-houseGenSettings.getDistanceFromOtherWiggle(), houseGenSettings.getDistanceFromOtherWiggle());
                currentDist = currentDist + distFromHouse;
                continue;
            }
            Polygon newPolygon = generateHouse(topleftX, topLeftY, angle, houseGenSettings);

            // Check the newly generated house with the hasher and make sure that it does not collide with existing
            // house
            boolean collide = false;
            for (Polygon p : hasher.getCollisionObjects(newPolygon)) {
                if (PhysicUtils.checkPolygonPolygonCollision(
                        p.getBoundaryPoints(), newPolygon.getBoundaryPoints())) {
                    collide = true;
                    break;
                };
            }

            // Add the polygon to the system, and reconfigure the settings to generate the next house.
            if (!collide) {
                polygons.add(newPolygon);
                hasher.addObject(newPolygon);
                lastPolygon = newPolygon;
            }
            double distFromHouse = houseGenSettings.getDistanceFromOther() +
                    MathUtils.randDouble(-houseGenSettings.getDistanceFromOtherWiggle(), houseGenSettings.getDistanceFromEdgeWiggle());
            currentDist = currentDist + distFromHouse;
            if (currentDist > roadDistance -  houseGenSettings.getDistanceFromCrossRoad()) break;
        }

        // Return the polygon list
        return polygons;
    }

    private Polygon generateUHouse(double topLeftX, double topLeftY, double angle, double width, double length) {
        // Randomize the house to be built in one of 4 possible direction
        double rand = MathUtils.randDouble(0, 1);
        HouseDirection houseDir;
        if (rand <= 0.25) {
            houseDir = HouseDirection.UP;
        } else if (rand <= 0.5) {
            houseDir = HouseDirection.RIGHT;
        } else if (rand <= 0.75) {
            houseDir = HouseDirection.DOWN;
        } else {
            houseDir = HouseDirection.LEFT;
        }


        // Move the top left point and the angle respectively
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        if (houseDir == HouseDirection.RIGHT) {
            topLeftX += sideUnitX * width * 1.0;
            topLeftY += sideUnitY * width * 1.0;
            angle += MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        } else if (houseDir == HouseDirection.DOWN) {
            topLeftX += sideUnitX * width * 1.0 + downUnitX * length * 1.0;
            topLeftY += sideUnitY * width * 1.0 + downUnitY * length * 1.0;
            angle += Math.PI;
        } else if (houseDir == HouseDirection.LEFT) {
            topLeftX += downUnitX * length * 1.0;
            topLeftY += downUnitY * length * 1.0;
            angle -= MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        }

        // Create the house
        return createPolygonBasedOnRatioPts(
                topLeftX, topLeftY, angle, width, length,
                new double[][] {
                        {0.0, 0.0},
                        {0.25, 0.0},
                        {0.25, 0.75},
                        {0.75, 0.75},
                        {0.75, 0.0},
                        {1.0, 0.0},
                        {1.0, 1.0},
                        {0.0, 1.0},
                });
    }

    /**
     * Generate an l-shape polygon that makes up a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon
     * The L-shape will fit perfect inside the rectangle, but will be facing in one of random 4 directions
     * (UP, DOWN, LEFT, RIGHT) relative to the given angle and will be either be a regular L or a flip L.
     */
    private Polygon generateLHouse(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        // Randomize the house to be built in one of 4 possible direction
        double rand = MathUtils.randDouble(0, 1);
        HouseDirection houseDir;
        if (rand <= 0.25) {
            houseDir = HouseDirection.UP;
        } else if (rand <= 0.5) {
            houseDir = HouseDirection.RIGHT;
        } else if (rand <= 0.75) {
            houseDir = HouseDirection.DOWN;
        } else {
            houseDir = HouseDirection.LEFT;
        }

        // Move the top left point and the angle respectively
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        if (houseDir == HouseDirection.RIGHT) {
            topLeftX += sideUnitX * width * 1.0;
            topLeftY += sideUnitY * width * 1.0;
            angle += MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        } else if (houseDir == HouseDirection.DOWN) {
            topLeftX += sideUnitX * width * 1.0 + downUnitX * length * 1.0;
            topLeftY += sideUnitY * width * 1.0 + downUnitY * length * 1.0;
            angle += Math.PI;
        } else if (houseDir == HouseDirection.LEFT) {
            topLeftX += downUnitX * length * 1.0;
            topLeftY += downUnitY * length * 1.0;
            angle -= MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        }

        // Randomly select whether the house will be a regular L or a flipped L.
        rand = MathUtils.randDouble(0, 1);
        if (rand < 0.5) {
            // Regular L shape
            return createPolygonBasedOnRatioPts(
                    topLeftX, topLeftY, angle, width, length,
                    new double[][] {
                            {0.0, 0.0},
                            {0.5, 0.0},
                            {0.5, 0.5},
                            {1.0, 0.5},
                            {1.0, 1.0},
                            {0.0, 1.0},
                    });
        } else {
            // Flipped L shape
            return createPolygonBasedOnRatioPts(
                    topLeftX, topLeftY, angle, width, length,
                    new double[][] {
                            {0.5, 0.0},
                            {1.0, 0.0},
                            {1.0, 1.0},
                            {0.0, 1.0},
                            {0.0, 0.5},
                            {0.5, 0.5},
                    });
        }
    }

    /**
     * Generate a polygon that fills a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon
     */
    private Polygon generateRectangleHouse(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        // Create the house
        return createPolygonBasedOnRatioPts(
                topLeftX, topLeftY, angle, width, length,
                new double[][] {
                        {0.0, 0.0},
                        {1.0, 0.0},
                        {1.0, 1.0},
                        {0.0, 1.0},
                });
    }

    /**
     * Generate an triangle polygon that fills a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon
     * The triangle will fit perfect inside the rectangle, but will be facing in one of random 4 directions
     * (UP, DOWN, LEFT, RIGHT);
     */
    private Polygon generateTriangleHouse(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        // Randomize the house to be built in one of 4 possible direction
        double rand = MathUtils.randDouble(0, 1);
        HouseDirection houseDir;
        if (rand <= 0.25) {
            houseDir = HouseDirection.UP;
        } else if (rand <= 0.5) {
            houseDir = HouseDirection.RIGHT;
        } else if (rand <= 0.75) {
            houseDir = HouseDirection.DOWN;
        } else {
            houseDir = HouseDirection.LEFT;
        }

        // Move the top left point and the house angle based on generated direction
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        if (houseDir == HouseDirection.RIGHT) {
            topLeftX += sideUnitX * width * 1.0;
            topLeftY += sideUnitY * width * 1.0;
            angle += MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        } else if (houseDir == HouseDirection.DOWN) {
            topLeftX += sideUnitX * width * 1.0 + downUnitX * length * 1.0;
            topLeftY += sideUnitY * width * 1.0 + downUnitY * length * 1.0;
            angle += Math.PI;
        } else if (houseDir == HouseDirection.LEFT) {
            topLeftX += downUnitX * length * 1.0;
            topLeftY += downUnitY * length * 1.0;
            angle -= MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        }

        // Create the house
        return createPolygonBasedOnRatioPts(
                topLeftX, topLeftY, angle, width, length,
                new double[][] {
                        {0, 0},
                        {1.0, 0},
                        {1.0, 1.0}
                });
    }

    /**
     * Generate an O-Shaped polygon that fills a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon
     * The house will fill the rectangle, but their gate will face one of 4 directions
     * (UP, DOWN, LEFT, RIGHT).
     */
    private Polygon generateOHouse(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        // Randomize the house to be built in one of 4 possible direction
        double rand = MathUtils.randDouble(0, 1);
        HouseDirection houseDir;
        if (rand <= 0.25) {
            houseDir = HouseDirection.UP;
        } else if (rand <= 0.5) {
            houseDir = HouseDirection.RIGHT;
        } else if (rand <= 0.75) {
            houseDir = HouseDirection.DOWN;
        } else {
            houseDir = HouseDirection.LEFT;
        }

        // Move the top left point and the house angle based on generated direction
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        if (houseDir == HouseDirection.RIGHT) {
            topLeftX += sideUnitX * width * 1.0;
            topLeftY += sideUnitY * width * 1.0;
            angle += MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        } else if (houseDir == HouseDirection.DOWN) {
            topLeftX += sideUnitX * width * 1.0 + downUnitX * length * 1.0;
            topLeftY += sideUnitY * width * 1.0 + downUnitY * length * 1.0;
            angle += Math.PI;
        } else if (houseDir == HouseDirection.LEFT) {
            topLeftX += downUnitX * length * 1.0;
            topLeftY += downUnitY * length * 1.0;
            angle -= MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        }

        // Create the house
        return createPolygonBasedOnRatioPts(
                topLeftX, topLeftY, angle, width, length,
                new double[][] {
                        {0, 0},
                        {0.45, 0},
                        {0.45, 0.2},
                        {0.2, 0.2},
                        {0.2, 0.8},
                        {0.8, 0.8},
                        {0.8, 0.2},
                        {0.55, 0.2},
                        {0.55, 0.0},
                        {1.0, 0},
                        {1.0, 1.0},
                        {0, 1.0},
                });
    }

    /**
     * Generate an T-Shaped polygon that fills a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon
     * The house will fill the rectangle, but their gate will face one of 4 directions
     * (UP, DOWN, LEFT, RIGHT).
     */
    private Polygon generateTHouse(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        // Randomize the house to be built in one of 4 possible direction
        double rand = MathUtils.randDouble(0, 1);
        HouseDirection houseDir;
        if (rand <= 0.25) {
            houseDir = HouseDirection.UP;
        } else if (rand <= 0.5) {
            houseDir = HouseDirection.RIGHT;
        } else if (rand <= 0.75) {
            houseDir = HouseDirection.DOWN;
        } else {
            houseDir = HouseDirection.LEFT;
        }

        // Move the top left point and the house angle based on generated direction
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        if (houseDir == HouseDirection.RIGHT) {
            topLeftX += sideUnitX * width * 1.0;
            topLeftY += sideUnitY * width * 1.0;
            angle += MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        } else if (houseDir == HouseDirection.DOWN) {
            topLeftX += sideUnitX * width * 1.0 + downUnitX * length * 1.0;
            topLeftY += sideUnitY * width * 1.0 + downUnitY * length * 1.0;
            angle += Math.PI;
        } else if (houseDir == HouseDirection.LEFT) {
            topLeftX += downUnitX * length * 1.0;
            topLeftY += downUnitY * length * 1.0;
            angle -= MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        }

        // Create the house
        return createPolygonBasedOnRatioPts(
                topLeftX, topLeftY, angle, width, length,
                new double[][] {
                        {0, 0},
                        {1.0, 0},
                        {1.0, 0.33},
                        {0.67, 0.33},
                        {0.67, 1.0},
                        {0.33, 1.0},
                        {0.33, 0.33},
                        {0.0, 0.33}
                });
    }

    /**
     * Generate an H-Shaped polygon that fills a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon
     * The house will fill the rectangle, but their gate will face one of 4 directions
     * (UP, DOWN, LEFT, RIGHT).
     * TODO: It is not necessary to have all 4 directions here, but it is indeed very nice to have this code replicable
     *  so that it can be easily copied and refactored. Refactor the direction selection code when you have sometime.
     */
    private Polygon generateHHouse(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        // Randomize the house to be built in one of 4 possible direction
        double rand = MathUtils.randDouble(0, 1);
        HouseDirection houseDir;
        if (rand <= 0.25) {
            houseDir = HouseDirection.UP;
        } else if (rand <= 0.5) {
            houseDir = HouseDirection.RIGHT;
        } else if (rand <= 0.75) {
            houseDir = HouseDirection.DOWN;
        } else {
            houseDir = HouseDirection.LEFT;
        }

        // Move the top left point and the house angle based on generated direction
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        if (houseDir == HouseDirection.RIGHT) {
            topLeftX += sideUnitX * width * 1.0;
            topLeftY += sideUnitY * width * 1.0;
            angle += MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        } else if (houseDir == HouseDirection.DOWN) {
            topLeftX += sideUnitX * width * 1.0 + downUnitX * length * 1.0;
            topLeftY += sideUnitY * width * 1.0 + downUnitY * length * 1.0;
            angle += Math.PI;
        } else if (houseDir == HouseDirection.LEFT) {
            topLeftX += downUnitX * length * 1.0;
            topLeftY += downUnitY * length * 1.0;
            angle -= MathUtils.PIO2;
            double temp = length;
            length = width;
            width = temp;
        }

        // Create the house
        return createPolygonBasedOnRatioPts(
                topLeftX, topLeftY, angle, width, length,
                new double[][] {
                        {0, 0},
                        {1.0, 0},
                        {1.0, 0.33},
                        {0.67, 0.33},
                        {0.67, 0.67},
                        {1.0, 0.67},
                        {1.0, 1.0},
                        {0.0, 1.0},
                        {0.0, 0.67},
                        {0.33, 0.67},
                        {0.33, 0.33},
                        {0.0, 0.33},
                });
    }

    /**
     * Generate a Plus-Shaped polygon that fills a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon.
     */
    private Polygon generatePlusHouse(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        return createPolygonBasedOnRatioPts(
                topLeftX, topLeftY, angle, width, length,
                new double[][] {
                        {0, 0.33},
                        {0.33, 0.33},
                        {0.33, 0.0},
                        {0.67, 0.0},
                        {0.67, 0.33},
                        {1.0, 0.33},
                        {1.0, 0.67},
                        {0.67, 0.67},
                        {0.67, 1.0},
                        {0.33, 1.0},
                        {0.33, 0.67},
                        {0.0, 0.67},
                });
    }

}
