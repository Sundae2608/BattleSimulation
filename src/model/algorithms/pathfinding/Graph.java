package model.algorithms.pathfinding;

import java.util.HashSet;

public class Graph {
    HashSet<Node> nodes;

    public Graph() {
        nodes = new HashSet<>();
    }

    /**
     * Add the node the graph.
     */
    public void addNode(Node node) {
        nodes.add(node);
    }

    /**
     * Connect the two nodes together.
     */
    public void connectNode(Node node1, Node node2) {
        node1.addAdjacentNode(node2);
        node2.addAdjacentNode(node1);
    }
}
