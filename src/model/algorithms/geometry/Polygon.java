package model.algorithms.geometry;

import java.util.ArrayList;
import java.util.HashSet;

public class Polygon {

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

    public Polygon(HashSet<Vertex> inputVertexs, HashSet<Edge> inputEdges) {
        vertices = inputVertexs;
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
        return orderedList;
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
