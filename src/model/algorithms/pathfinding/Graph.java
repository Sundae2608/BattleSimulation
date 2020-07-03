package model.algorithms.pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Graph {
    HashMap<Integer, Node> nodes;

    public Graph() {
        nodes = new HashMap<>();
    }

    public Graph(HashMap<Integer, double[]> inputNodes, ArrayList<int[]> edges) {
        nodes = new HashMap<>();
        for (int id : inputNodes.keySet()) {
            double[] nodePt = inputNodes.get(id);
            nodes.put(id, new Node(nodePt[0], nodePt[1]));
        }
        for (int[] edge : edges) {
            connectNode(nodes.get(edge[0]), nodes.get(edge[1]));
        }
    }
    /**
     * Connect the two nodes together.
     */
    private void connectNode(Node node1, Node node2) {
        node1.addAdjacentNode(node2);
        node2.addAdjacentNode(node1);
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }
}
