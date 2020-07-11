package model.algorithms.pathfinding;

import model.utils.MathUtils;

import java.util.HashMap;

public class Node {
    double x;
    double y;
    HashMap<Node, Double> adjacentNodes;

    public Node(double inputX, double inputY) {
        x = inputX;
        y = inputY;
        adjacentNodes = new HashMap<>();
    }

    public void addAdjacentNode(Node node) {
        double distance = MathUtils.quickDistance(x, y, node.getX(), node.getY());
        adjacentNodes.put(node, distance);
    }

    public void removeAdjacentNode(Node node) {
        if (adjacentNodes.containsKey(node)) {
            adjacentNodes.remove(node);
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public HashMap<Node, Double> getAdjacentNodes() {
        return adjacentNodes;
    }
}
