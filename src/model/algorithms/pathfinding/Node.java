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

    public double[] getPt() {
        return new double[] {x, y};
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public HashMap<Node, Double> getAdjacentNodes() {
        return adjacentNodes;
    }

    @Override
    public boolean equals(Object o)
    {
        // TODO: Overall all of edge graphing system so that we compare systematically from address to content.
        // Checks specified object is "equal to" current object or not
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Node node = (Node) o;

        // If they are not the same memory, check if they are the same node.
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = Double.valueOf(x).hashCode();
        result = 31 * result + Double.valueOf(y).hashCode();
        return result;
    }
}
