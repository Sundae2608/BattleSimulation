package model.algorithms.geometry;

import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Polygon {

    private static final int PERCEPTUAL_ANGLE_SEARCH_GRANULARITY = 720;

    private HashSet<Vertex> vertices;
    private HashSet<Edge> edges;
    private EntityType entityType;

    public Polygon() {
        vertices = new HashSet<>();
        edges = new HashSet<>();
        entityType = EntityType.DEFAULT;
    }

    public Polygon(HashSet<Edge> inputEdges) {
        edges = inputEdges;
        vertices = new HashSet<>();
        for (Edge e : inputEdges) {
            vertices.add(e.vertex1);
            vertices.add(e.vertex2);
        }
        entityType = EntityType.DEFAULT;
    }

    public Polygon(HashSet<Vertex> inputVertices, HashSet<Edge> inputEdges) {
        vertices = inputVertices;
        edges = inputEdges;
        entityType = EntityType.DEFAULT;
    }

    public HashSet<Vertex> getVertices() {
        return vertices;
    }

    public HashSet<Edge> getEdges() {
        return edges;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * Return the list of edges in the order that makes them connect to each other and becomes polygons.
     * The vertices will be in counter-clockwise order.
     */
    public ArrayList<Vertex> getOrderedVertices() {
        ArrayList<Vertex> orderedList = new ArrayList<>();
        HashSet<Vertex> visitedVertex = new HashSet<>();
        if (edges.size() == 0) {
            return orderedList;
        }
        Edge currEdge = (Edge) edges.toArray()[0];
        Vertex currVertex = currEdge.vertex1;
        while (orderedList.size() != edges.size()) {
            orderedList.add(currVertex);
            visitedVertex.add(currVertex);
            boolean found = false;
            for (Edge edge : edges) {
                if (edge != currEdge && edge.getVertex1() == currVertex && !visitedVertex.contains(edge.getVertex2())) {
                    currEdge = edge;
                    currVertex = edge.getVertex2();
                    found = true;
                    break;
                }
                if (edge != currEdge && edge.getVertex2() == currVertex && !visitedVertex.contains(edge.getVertex1())) {
                    currEdge = edge;
                    currVertex = edge.getVertex1();
                    found = true;
                    break;
                }
            }
            if (!found) break;
        }

        // Calculate the sum of angles, and possibly reverse the list of ordering based on this sum to make sure the
        // vertices are returned in clockwise order.
        double sumAngle = 0;
        int numVertices = orderedList.size();
        for (int i = 0; i < numVertices; i++) {
            Vertex v1 = orderedList.get(i);
            Vertex v2 = orderedList.get((i + 1) % numVertices);
            Vertex v3 = orderedList.get((i + 2) % numVertices);
            sumAngle += MathUtils.angleFromPts(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
        }

        // If the sum of angle is equal to (n + 2) * PI, then we should reverse the ordered list.
        if (MathUtils.doubleEqual(sumAngle, (numVertices + 2) * Math.PI)) {
            Collections.reverse(orderedList);
        }
        return orderedList;
    }

    /**
     * Get bounding box dimensions according given angle.
     * The order of points in the bounding box will be as followed
     * (topLeft -> bottomLeft -> bottomRight -> top Right)
     */
    public double[][] getBoundingBoxDimensions(double angle) {

        // Find minX, maxX, minY, maxY after all the point has been rotated by some angle.
        ArrayList<Vertex> verticesList = new ArrayList<>(vertices);
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (Vertex vertex : verticesList) {
            double[] rotatedVertex = MathUtils.rotatePointByAngle(vertex.x, vertex.y, angle);
            double x = rotatedVertex[0];
            double y = rotatedVertex[1];
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        // Initialize array containing bounding box points, rotate it back to the original angle.
        double[][] pts = new double[][] {
                {minX, minY}, {minX, maxY}, {maxX, maxY}, {maxX, minY}
        };
        for (int i = 0; i < 4; i++) {
            double[] rotatedVertex = MathUtils.rotatePointByAngle(pts[i][0], pts[i][1], -angle);
            double x = rotatedVertex[0];
            double y = rotatedVertex[1];
            pts[i] = new double[] {x, y};
        }
        return pts;
    }

    /**
     * Get perceptual angle of the polygon. Perceptual angle is the angle that a normal person would see the polygon if
     * the polygon is roughly seen as a rectangle. This angle is extremely useful when dividing polygons
     */
    public double getPerceptualAngle() {
        // Get the list of vertices in array list form
        ArrayList<Vertex> verticesList = new ArrayList<>(vertices);

        // Go through the angles from 0 to Math.PI. The angle that returns the smallest area will be the "perceptual
        // angle" of the polygon.
        double minArea = Double.MAX_VALUE;
        double angleOfMin = 0;
        double widthOfMin = 0;
        double lengthOfMin = 0;
        for (int i = 0; i < PERCEPTUAL_ANGLE_SEARCH_GRANULARITY; i++) {
            double angle = MathUtils.PIO2 * i / PERCEPTUAL_ANGLE_SEARCH_GRANULARITY;
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            for (Vertex vertex : verticesList) {
                // TODO: Refactor this into a rotate angle function in MathUtils.
                double x = vertex.getX() * MathUtils.quickCos((float) angle) + vertex.getY() * MathUtils.quickSin((float) angle);
                double y = -vertex.getX() * MathUtils.quickSin((float) angle) + vertex.getY() * MathUtils.quickCos((float) angle);
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
            double area = (maxY - minY) * (maxX - minX);
            if (area < minArea) {
                minArea = area;
                angleOfMin = angle;
                widthOfMin = maxX - minX;
                lengthOfMin = maxY - maxX;
            }
        }
        if (widthOfMin < lengthOfMin) angleOfMin += MathUtils.PIO2;
        return angleOfMin;
    }

    /**
     * Get an array of boundary points from the polygon.
     */
    public double[][] getBoundaryPoints() {
        ArrayList<Vertex> orderVertices = getOrderedVertices();
        double[][] pts = new double[orderVertices.size()][2];
        for (int i = 0; i < orderVertices.size(); i++) {
            pts[i][0] = orderVertices.get(i).getX();
            pts[i][1] = orderVertices.get(i).getY();
        }
        return pts;
    }

    public double[] getCenterOfMass() {
        double sumX = 0;
        double sumY = 0;
        int count = 0;
        for (Vertex vertex : vertices) {
            count += 1;
            sumX += vertex.getX();
            sumY += vertex.getY();
        }
        return new double[] {sumX / count, sumY / count};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Polygon polygon = (Polygon) o;

        // Polygons are equal if each edge in the first polygon is equal to exactly one edge in the second polygon.
        HashSet<Edge> otherEdges = ((Polygon) o).getEdges();
        if (otherEdges.size() != edges.size()) return false;
        for (Edge edge : edges) {
            if (!otherEdges.contains(edge)) return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 0;
        for (Edge edge : edges) {
            result = result ^ edge.hashCode();
        }
        return result;
    }
}
