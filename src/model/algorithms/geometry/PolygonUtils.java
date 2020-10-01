package model.algorithms.geometry;

import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.*;

public class PolygonUtils {

    // Extra padding to ensure the line cuts through the polygons
    private final static int POLYGON_CUTTING_PADDING = 10;

    /**
     * Use BFS to the shortest path from one polygon to another polygon.
     * We use this search algorithm to construct a list of polygons that would make a river.
     * TODO: Rename the river object.
     * @param riverBegin The beginning polygon.
     * @param riverEnd The ending polygon.
     * @return A list of polygon (not necessary in order) that constructs the path from beginning to the end.
     */
    public static List<Polygon> findRiverPathBFS(PolygonSystem polygonSystem, Polygon riverBegin, Polygon riverEnd) {
        HashSet<Polygon> visited = new HashSet<>();
        Queue<List<Polygon>> pathQueue = new LinkedList<>();
        List<Polygon> path = new ArrayList<>();
        path.add(riverBegin);
        pathQueue.add(path);
        visited.add(riverBegin);

        while (!pathQueue.isEmpty()) {
            List<Polygon> currentPath = pathQueue.poll();
            Polygon currentPathEnd = currentPath.get(currentPath.size()-1);

            if (currentPathEnd == riverEnd) {
                return currentPath;
            }

            for (Edge currentEdge : currentPathEnd.getEdges()) {
                for (Polygon nextPolygon : polygonSystem.getAdjacentPolygon(currentEdge)) {
                    if (!visited.contains(nextPolygon) && nextPolygon.getEntityType() == EntityType.DEFAULT) {
                        visited.add(nextPolygon);
                        List<Polygon> nextPath = new ArrayList<>(currentPath);
                        nextPath.add(nextPolygon);
                        pathQueue.add(nextPath);
                    }
                }
            }
        }
        return path;
    }

    /**
     * Divide polygon into n vertical strips
     */
    public static ArrayList<Polygon> dividePolygonsUsingVerticalStrip(Polygon p, int numRows, int numCols) {
        // Get the perceptual angle and the bounding box that goes with that angle.
        double angle = p.getPerceptualAngle();
        double[][] boundingBoxes = p.getBoundingBoxDimensions(angle);

        // Create lines that divide the bounding box into given n strips.
        double sideUnitX = MathUtils.quickCos((float) angle);
        double sideUnitY = MathUtils.quickSin((float) angle);
        double downUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double downUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        double topLeftX = boundingBoxes[0][0];
        double topLeftY = boundingBoxes[0][1];
        double bottomLeftX = boundingBoxes[1][0];
        double bottomLeftY = boundingBoxes[1][1];
        double topRightX = boundingBoxes[3][0];
        double topRightY = boundingBoxes[3][1];
        double length = MathUtils.distance(
                topLeftX, topLeftY, topRightX, topRightY);
        double width = MathUtils.distance(
                topLeftX, topLeftY, bottomLeftX, bottomLeftY);

        // Construct cutting line and then proceed to cut the polygons row by row, and then column by column
        ArrayList<Polygon> recentlySplitedPolygons = new ArrayList<>();
        recentlySplitedPolygons.add(p);
        for (int i = 1; i < numRows; i++) {
            double x1 = topLeftX + sideUnitX * length * i / numRows - downUnitX * POLYGON_CUTTING_PADDING;
            double y1 = topLeftY + sideUnitY * length * i / numRows - downUnitY * POLYGON_CUTTING_PADDING;
            double x2 = bottomLeftX + sideUnitX * length * i / numRows + downUnitX * POLYGON_CUTTING_PADDING;
            double y2 = bottomLeftY + sideUnitY * length * i / numRows + downUnitY * POLYGON_CUTTING_PADDING;
            ArrayList<Polygon> newPolygons = new ArrayList<>();
            for (Polygon currPolygon : recentlySplitedPolygons) {
                for (Polygon newlyCutPolygon : cutPolygonIntoTwoByLine(currPolygon, x1, y1, x2, y2)) {
                    newPolygons.add(newlyCutPolygon);
                }
            }
            recentlySplitedPolygons = newPolygons;
        }
        for (int i = 1; i < numCols; i++) {
            double x1 = topLeftX + downUnitX * width * i / numCols - sideUnitX * POLYGON_CUTTING_PADDING;
            double y1 = topLeftY + downUnitY * width * i / numCols - sideUnitY * POLYGON_CUTTING_PADDING;
            double x2 = topRightX + downUnitX * width * i / numCols + sideUnitX * POLYGON_CUTTING_PADDING;
            double y2 = topRightY + downUnitY * width * i / numCols + sideUnitY * POLYGON_CUTTING_PADDING;
            ArrayList<Polygon> newPolygons = new ArrayList<>();
            for (Polygon currPolygon : recentlySplitedPolygons) {
                for (Polygon newlyCutPolygon : cutPolygonIntoTwoByLine(currPolygon, x1, y1, x2, y2)) {
                    newPolygons.add(newlyCutPolygon);
                }
            }
            recentlySplitedPolygons = newPolygons;
        }
        return recentlySplitedPolygons;
    }

    /**
     * Cut the current polygon into 2 by a line defined by (x1, y1) and (x2, y2).
     * The polygon will not be cut if and the list contain one same polygon will return if
     * + The line does not cut through the polygon
     * + The line cut through the polygon in more than 2 points.
     */
    public static ArrayList<Polygon> cutPolygonIntoTwoByLine(Polygon p, double x1, double y1, double x2, double y2) {

        // Add the indices of the lines in which the line (x1, y1) - (x2, y2) cuts.
        ArrayList<Vertex> vertices = p.getOrderedVertices();
        int numPts = vertices.size();
        ArrayList<Integer> indicesOfCutLines = new ArrayList<Integer>();
        for (int i = 0; i < numPts; i++) {
            Vertex v1 = vertices.get(i);
            Vertex v2 = vertices.get((i + 1) % numPts);
            if (PhysicUtils.checkLineLineCollision(x1, y1, x2, y2, v1.x, v1.y, v2.x, v2.y)) {
                indicesOfCutLines.add(i);
            }
        }

        // If there are more or less than exactly two lines cut, return the original polygon
        ArrayList<Polygon> returnPolygons = new ArrayList<>();
        if (indicesOfCutLines.size() <= 1 || indicesOfCutLines.size() >= 3) {
            returnPolygons.add(p);
            return returnPolygons;
        }

        // If there are exactly two lines cut, we will first add the cut points, and then construct new polygons
        // using said points.
        int cutIndex1 = indicesOfCutLines.get(0);
        int cutIndex2 = indicesOfCutLines.get(1);
        double[] cutPt1 = PhysicUtils.getLineLineIntersection(
                x1, y1,
                x2, y2,
                vertices.get(cutIndex1).x,
                vertices.get(cutIndex1).y,
                vertices.get((cutIndex1 + 1) % numPts).x,
                vertices.get((cutIndex1 + 1) % numPts).y);
        Vertex sharedV1 = new Vertex(cutPt1[0], cutPt1[1]);
        double[] cutPt2 = PhysicUtils.getLineLineIntersection(
                x1, y1,
                x2, y2,
                vertices.get(cutIndex2).x,
                vertices.get(cutIndex2).y,
                vertices.get((cutIndex2 + 1) % numPts).x,
                vertices.get((cutIndex2 + 1) % numPts).y);
        Vertex sharedV2 = new Vertex(cutPt2[0], cutPt2[1]);

        // Create two new polygons based on new edges.
        int curr = cutIndex1 + 1;
        ArrayList<Vertex> p1Vertices = new ArrayList<>();
        while (curr != (cutIndex2 + 1) % numPts) {
            p1Vertices.add(vertices.get(curr));
            curr = (curr + 1) % numPts;
        }
        p1Vertices.add(sharedV2);
        p1Vertices.add(sharedV1);

        ArrayList<Vertex> p2Vertices = new ArrayList<>();
        while (curr != (cutIndex1 + 1) % numPts) {
            p2Vertices.add(vertices.get(curr));
            curr = (curr + 1) % numPts;
        }
        p2Vertices.add(sharedV1);
        p2Vertices.add(sharedV2);

        // Construct the two polygons and return the two polygons.
        Edge sharedEdge = new Edge(sharedV1, sharedV2);
        HashSet<Edge> edges1 = new HashSet<>();
        for (int i = 0; i < p1Vertices.size(); i++) {
            Vertex v1 = p1Vertices.get(i);
            Vertex v2 = p1Vertices.get((i + 1) % p1Vertices.size());
            if (v1 != sharedV2) {
                edges1.add(new Edge(v1, v2));
            } else {
                edges1.add(sharedEdge);
            }
        }
        returnPolygons.add(new Polygon(edges1));

        HashSet<Edge> edges2 = new HashSet<>();
        for (int i = 0; i < p2Vertices.size(); i++) {
            Vertex v1 = p2Vertices.get(i);
            Vertex v2 = p2Vertices.get((i + 1) % p2Vertices.size());
            if (v1 != sharedV1) {
                edges2.add(new Edge(v1, v2));
            } else {
                edges2.add(sharedEdge);
            }
        }
        returnPolygons.add(new Polygon(edges2));

        // Return the polygons
        return returnPolygons;
    }
}
