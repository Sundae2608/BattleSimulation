package model.algorithms.geometry;

import model.algorithms.pathfinding.Node;

import java.util.ArrayList;
import java.util.HashSet;

public class Polygon {

    private HashSet<Node> nodes;
    private HashSet<Edge> edges;
    private EntityType entityType;

    public Polygon() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        entityType = EntityType.DEFAULT;
    }

    public Polygon(HashSet<Edge> inputEdges) {
        edges = inputEdges;
        nodes = new HashSet<>();
        for (Edge e : inputEdges) {
            nodes.add(e.node1);
            nodes.add(e.node2);
        }
        entityType = EntityType.DEFAULT;
    }

    public Polygon(HashSet<Node> inputNodes, HashSet<Edge> inputEdges) {
        nodes = inputNodes;
        edges = inputEdges;
        entityType = EntityType.DEFAULT;
    }

    public HashSet<Node> getNodes() {
        return nodes;
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
    public ArrayList<Node> getOrderedNodes() {
        ArrayList<Node> orderedList = new ArrayList<>();
        HashSet<Node> visitedNode = new HashSet<>();
        if (edges.size() == 0) {
            return orderedList;
        }
        Edge currEdge = (Edge) edges.toArray()[0];
        Node currNode = currEdge.node1;
        while (orderedList.size() != edges.size()) {
            orderedList.add(currNode);
            visitedNode.add(currNode);
            boolean found = false;
            for (Edge edge : edges) {
                if (edge != currEdge && edge.getNode1() == currNode && !visitedNode.contains(edge.getNode2()) && !found) {
                    currEdge = edge;
                    currNode = edge.getNode2();
                    found = true;
                    break;
                }
                if (edge != currEdge && edge.getNode2() == currNode && !visitedNode.contains(edge.getNode1()) && !found) {
                    currEdge = edge;
                    currNode = edge.getNode1();
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
        ArrayList<Node> orderedNodes = getOrderedNodes();
        double[][] pts = new double[orderedNodes.size()][2];
        for (int i = 0; i < orderedNodes.size(); i++) {
            pts[i][0] = orderedNodes.get(i).getX();
            pts[i][1] = orderedNodes.get(i).getY();
        }
        return pts;
    }

    public double[] getCenterOfMass() {
        double sumX = 0;
        double sumY = 0;
        int count = 0;
        for (Node node : nodes) {
            count += 1;
            sumX += node.getX();
            sumY += node.getY();
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
