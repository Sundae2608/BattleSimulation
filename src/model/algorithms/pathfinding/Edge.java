package model.algorithms.pathfinding;

public class Edge {

    // Each edge contains two nodes
    Node node1;
    Node node2;

    public Edge(Node inputNode1, Node inputNode2) {
        node1 = inputNode1;
        node2 = inputNode2;
    }
}
