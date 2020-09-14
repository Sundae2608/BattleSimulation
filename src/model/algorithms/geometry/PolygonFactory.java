package model.algorithms.geometry;

import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PolygonFactory {

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
     * Create a list of rectangular polygons representing houses along the edge, which supposedly represent the streets.
     * @param e The edge in question
     * @param settings Settings to generate house
     * @param hasher
     * @return
     */
    public ArrayList<Polygon> createHousePolygonsFromEdge(
            Edge e, HouseGenerationSettings settings, PolygonHasher hasher) {
        // Pre-calculate  several directional information
        Vertex vertex1 = e.getVertex1();
        Vertex vertex2 = e.getVertex2();
        double roadDistance = MathUtils.quickDistance(vertex1.x, vertex1.y, vertex2.x, vertex2.y);
        double angle = MathUtils.atan2(vertex2.y - vertex1.y, vertex2.x - vertex1.x);
        double beginX = vertex1.x;
        double beginY = vertex1.y;
        double roadUnitX = MathUtils.quickCos((float) angle);
        double roadUnitY = MathUtils.quickSin((float) angle);
        double rightSideUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double rightSideUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        double leftSideUnitX = MathUtils.quickCos((float) (angle - MathUtils.PIO2));
        double leftSideUnitY = MathUtils.quickSin((float) (angle - MathUtils.PIO2));

        // Generate the left-side houses
        ArrayList<Polygon> polygons = new ArrayList<>();
        double currentDist = settings.getHouseWidth();
        while (true) {
            // Generate a house if possible
            double width = settings.getHouseWidth() +
                    MathUtils.randDouble(-settings.getHouseWidthWiggle(), settings.getHouseWidthWiggle());
            if (currentDist + width > roadDistance - settings.getHouseWidth()) break;
            double area = settings.getHouseArea() +
                    MathUtils.randDouble(-settings.getHouseAreaWiggle(), settings.getHouseAreaWiggle());
            double length = area / width;
            System.out.println(String.valueOf(width) + " " + String.valueOf(length));
            double distFromEdge = settings.getDistanceFromEdge() +
                    MathUtils.randDouble(-settings.getDistanceFromEdgeWiggle(), settings.getDistanceFromEdgeWiggle());
            Polygon polygon = generateHouseBoundaryPoints(
                    beginX + roadUnitX * currentDist + distFromEdge * rightSideUnitX,
                    beginY + roadUnitY * currentDist + distFromEdge * rightSideUnitY,
                    angle, width, length);
            polygon.setEntityType(EntityType.HOUSE);

            // Check the newly generated house with the hasher and make sure that it does not collide with existing
            // house
            boolean collide = false;
            for (Polygon p : hasher.getCollisionObjects(polygon)) {
                if (PhysicUtils.checkPolygonPolygonCollision(
                        p.getBoundaryPoints(), polygon.getBoundaryPoints())) {
                    collide = true;
                    break;
                };
            }
            if (!collide) {
                polygons.add(polygon);
                hasher.addObject(polygon);
            }
            double distFromHouse = settings.getDistanceFromOther() +
                    MathUtils.randDouble(-settings.getDistanceFromOtherWiggle(), settings.getDistanceFromEdgeWiggle());
            currentDist = currentDist + width + distFromHouse;
        }

        // Generate the right-side houses
        currentDist = settings.getHouseWidth();
        beginX = vertex2.x;
        beginY = vertex2.y;
        angle = angle + Math.PI;
        while (true) {
            // Generate a house if possible
            double width = settings.getHouseWidth() +
                    MathUtils.randDouble(-settings.getHouseWidthWiggle(), settings.getHouseWidthWiggle());
            if (currentDist + width > roadDistance - settings.getHouseWidth()) break;
            double area = settings.getHouseArea() +
                    MathUtils.randDouble(-settings.getHouseAreaWiggle(), settings.getHouseAreaWiggle());
            double length = area / width;
            System.out.println(String.valueOf(width) + " " + String.valueOf(length));
            double distFromEdge = settings.getDistanceFromEdge() +
                    MathUtils.randDouble(-settings.getDistanceFromEdgeWiggle(), settings.getDistanceFromEdgeWiggle());
            Polygon polygon = generateHouseBoundaryPoints(
                    beginX - roadUnitX * currentDist + distFromEdge * leftSideUnitX,
                    beginY - roadUnitY * currentDist + distFromEdge * leftSideUnitY,
                    angle, width, length);
            polygon.setEntityType(EntityType.HOUSE);

            // Check the newly generated house with the hasher and make sure that it does not collide with existing
            // house
            boolean collide = false;
            for (Polygon p : hasher.getCollisionObjects(polygon)) {
                if (PhysicUtils.checkPolygonPolygonCollision(
                        p.getBoundaryPoints(), polygon.getBoundaryPoints())) {
                    collide = true;
                    break;
                };
            }

            // Add the polygon to the system, and reconfigure the settings to generate the next house.
            if (!collide) {
                polygons.add(polygon);
                hasher.addObject(polygon);
            }
            double distFromHouse = settings.getDistanceFromOther() +
                    MathUtils.randDouble(-settings.getDistanceFromOtherWiggle(), settings.getDistanceFromEdgeWiggle());
            currentDist = currentDist + width + distFromHouse;
        }

        // Return the polygon list
        return polygons;
    }

    /**
     * Generate a polygon that that makes up a rectangle based on:
     * + point (x, y) as the top left corner of the polygon.
     * + angle as the angle of the rectangle facing downward.
     * + (width, length) as the dimension of that polygon
     */
    private Polygon generateHouseBoundaryPoints(
            double topLeftX, double topLeftY, double angle, double width, double length) {
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        Vertex v1 = new Vertex(topLeftX, topLeftY);
        Vertex v2 = new Vertex(
                topLeftX + width * sideUnitX,
                topLeftY + width * sideUnitY);
        Vertex v3 = new Vertex(
                topLeftX + width * sideUnitX + length * downUnitX,
                topLeftY + width * sideUnitY + length * downUnitY);
        Vertex v4 = new Vertex(
                topLeftX + length * downUnitX,
                topLeftY + length * downUnitY);
        Edge e1 = new Edge(v1, v2);
        Edge e2 = new Edge(v2, v3);
        Edge e3 = new Edge(v3, v4);
        Edge e4 = new Edge(v4, v1);
        HashSet<Vertex> vertexSet = new HashSet<>();
        HashSet<Edge> edgeSet = new HashSet<>();
        vertexSet.add(v1); vertexSet.add(v2); vertexSet.add(v3); vertexSet.add(v4);
        edgeSet.add(e1); edgeSet.add(e2); edgeSet.add(e3) ;edgeSet.add(e4);
        Polygon p = new Polygon(vertexSet, edgeSet);
        return p;
    }
}
