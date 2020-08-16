package model.algorithms.pathfinding;

import model.utils.Triplet;

public class Edge {

    // Each edge contains two nodes
    Node node1;
    Node node2;

    public Edge(Node inputNode1, Node inputNode2) {
        // TODO: Make edge hashable
        node1 = inputNode1;
        node2 = inputNode2;
    }

    @Override
    public boolean equals(Object o)
    {
        // TODO: Overall all of edge graphing system so that we compare systematically from address to content.
        // Checks specified object is "equal to" current object or not
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Edge edge = (Edge) o;

        // If they are not the same memory, check if they are the same node.
        return (node1 == edge.node1 && node2 == edge.node2) || (node1 == edge.node2 && node2 == edge.node1);
    }

    @Override
    public int hashCode()
    {
		int result = node1.hashCode() ^ node2.hashCode();
		return result;
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }
}
