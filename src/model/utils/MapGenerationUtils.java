package model.utils;

import model.algorithms.geometry.Edge;
import model.algorithms.pathfinding.Node;
import model.algorithms.geometry.Polygon;

import java.util.ArrayList;
import java.util.HashSet;

public final class MapGenerationUtils {

    /**
     * Merge two polygons together
     */
    public static Polygon mergePolygon(Polygon p1, Polygon p2) {
        // Get the list of edges that are shared between the two polygons.
        HashSet<Edge> sharedEdges = new HashSet<>(p1.getEdges());
        sharedEdges.retainAll(p2.getEdges());

        // The new polygon will share all edges except the shared edges, and all the nodes.
        HashSet<Edge> newEdges = new HashSet<>();
        for (Edge e : p1.getEdges()) {
            newEdges.add(e);
        }
        for (Edge e : p2.getEdges()) {
            newEdges.add(e);
        }
        for (Edge e : sharedEdges) {
            newEdges.remove(e);
        }

        // Get all the nodes from the edges
        HashSet<Node> nodes = new HashSet<>();
        for (Edge e : newEdges) {
            nodes.add(e.getNode1());
            nodes.add(e.getNode2());
        }
        return new Polygon(nodes, newEdges);
    }

    /**
     * Merge multiple polygons together
     */
    public static Polygon mergeMultiplePolygons(ArrayList<Polygon> polygons) {
        HashSet<Edge> uniqueEdges = new HashSet<>();
        HashSet<Edge> duplicatedEdges = new HashSet<>();
        for (Polygon polygon : polygons) {
            for (Edge edge : polygon.getEdges()) {
                if (duplicatedEdges.contains(edge)) continue;
                if (uniqueEdges.contains(edge)) {
                    uniqueEdges.remove(edge);
                    duplicatedEdges.add(edge);
                    continue;
                }
                uniqueEdges.add(edge);
            }
        }

        // Get all the nodes from the edges
        HashSet<Node> nodes = new HashSet<>();
        for (Edge e : uniqueEdges) {
            nodes.add(e.getNode1());
            nodes.add(e.getNode2());
        }

        // Return the polygon
        return new Polygon(nodes, uniqueEdges);
    }
}
