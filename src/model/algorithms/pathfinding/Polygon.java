package model.algorithms.pathfinding;

import java.util.HashSet;

public class Polygon {

    HashSet<Node> nodes;
    HashSet<Edge> edges;

    public Polygon() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
    }

    public Polygon(Triangle triangle) {
        nodes = new HashSet<>();
        nodes.add(triangle.getNode1());
        nodes.add(triangle.getNode2());
        nodes.add(triangle.getNode3());

        edges = new HashSet<>();
        edges.add(new Edge(triangle.getNode1(), triangle.getNode2()));
        edges.add(new Edge(triangle.getNode1(), triangle.getNode3()));
        edges.add(new Edge(triangle.getNode2(), triangle.getNode3()));
    }

    public Polygon(HashSet<Node> inputNodes, HashSet<Edge> inputEdges) {
        nodes = inputNodes;
        edges = inputEdges;
    }

    public HashSet<Node> getNodes() {
        return nodes;
    }

    public HashSet<Edge> getEdges() {
        return edges;
    }

    public double[] getCenterOfMass() {
        double avgX = 0;
        double avgY = 0;
        int count = 0;
        for (Node node : nodes) {
            count += 1;
            avgX = avgX * (count - 1) / count + node.getX() / count;
            avgY = avgY * (count - 1) / count + node.getY() / count;
        }
        return new double[] { avgX, avgY };
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
