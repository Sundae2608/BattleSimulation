package model.algorithms.pathfinding;

import model.construct.Construct;
import model.utils.PhysicUtils;
import utils.ConfigUtils;

import java.lang.reflect.Array;
import java.util.*;

public class Graph {

    // Hash map connecting integer ID to node.
    private HashMap<Integer, Node> nodes;

    // Begin node and end node, this is used to serve
    private Node beginNode;
    private Node endNode;

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

    /**
     * Remove connection out of and into input node.
     */
    private void removeAllConnectionsToNode(Node node) {
        for (Node node1 : nodes.values()) {
            node1.removeAdjacentNode(node);
        }
    }

    /**
     * Get the list of all nodes.
     */
    public Collection<Node> getNodes() {
        return nodes.values();
    }

    /**
     * Reset begin node and end node.
     * @param inputBeginNode Begin node, which represents where the unit is in the map.
     * @param inputEndNode End node, which represents where the unit is in the map.
     * @param constructs Constructs on the map. Edge that goes through a construct is illegal.
     */
    private void resetBeginAndEndNode(Node inputBeginNode, Node inputEndNode, ArrayList<Construct> constructs) {
        // Remove all connection to the currently stored begin node and end node
        removeAllConnectionsToNode(beginNode);
        removeAllConnectionsToNode(endNode);

        // Set the begin node and end node to the newly input two node
        beginNode = inputBeginNode;
        endNode = inputEndNode;

        // Check the collision between the two line and each construct.
        for (Node node : nodes.values()) {
            boolean cutConstructs = false;
            for (Construct construct : constructs) {
                if (PhysicUtils.checkLinePolygonCollision(beginNode.x, beginNode.y, node.x, node.y, construct.getBoundaryPoints())) {
                    cutConstructs = true;
                    break;
                }
            }
            if (!cutConstructs) {
                connectNode(beginNode, node);
            }
        }
        for (Node node : nodes.values()) {
            boolean cutConstructs = false;
            for (Construct construct : constructs) {
                if (PhysicUtils.checkLinePolygonCollision(endNode.x, endNode.y, node.x, node.y, construct.getBoundaryPoints())) {
                    cutConstructs = true;
                    break;
                }
            }
            if (!cutConstructs) {
                connectNode(endNode, node);
            }
        }
        boolean cutConstructs = false;
        for (Construct construct : constructs) {
            if (PhysicUtils.checkLinePolygonCollision(beginNode.x, beginNode.y, endNode.x, endNode.y, construct.getBoundaryPoints())) {
                cutConstructs = true;
                break;
            }
        }
        if (!cutConstructs) {
            connectNode(beginNode, endNode);
        }
    }

    /**
     * Find the note with the lowest distance.
     */
    private Node getLowestDistanceNode(Set<Node> unsettledNodes, HashMap<Node, Double> distanceMap) {
        Node lowestDistanceNode = null;
        double lowestDistance = Double.MAX_VALUE;
        for (Node node: unsettledNodes) {
            double nodeDistance = distanceMap.get(node);
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    /**
     * Calculate the minimum distance from the source node to the evaluation node.
     */
    private void calculateMinimumDistance(Node evaluationNode,
                                          Double edgeWeight, Node sourceNode,
                                          HashMap<Node, Double> distanceMap,
                                          HashMap<Node, Path> shortestPathsMap) {
        Double sourceDistance = distanceMap.get(sourceNode);
        if (sourceDistance + edgeWeight < distanceMap.get(evaluationNode)) {
            distanceMap.put(evaluationNode, sourceDistance + edgeWeight);
            Path shortestPath = new Path(shortestPathsMap.get(sourceNode));
            shortestPath.addNode(sourceNode);
            shortestPathsMap.put(evaluationNode, shortestPath);
        }
    }

    public Path getShortestPath(double x1, double y1, double x2, double y2, ArrayList<Construct> constructs) {
        // Add start node and end node to the graph.
        Node beginNode = new Node(x1, y1);
        Node endNode = new Node(x2, y2);
        resetBeginAndEndNode(beginNode, endNode, constructs);

        // Initialize distance map, which maps each node to the distance to the begin node. Naturally, distance
        // to beginNode is 0..
        HashMap<Node, Double> distanceMap = new HashMap<>();
        for (Node node : this.getNodes()) {
            distanceMap.put(node, Double.MAX_VALUE);
        }
        distanceMap.put(beginNode, Double.MAX_VALUE);
        distanceMap.put(endNode, Double.MAX_VALUE);

        HashMap<Node, Path> shortestPathsMap = new HashMap<>();
        for (Node node : this.getNodes()) {
            shortestPathsMap.put(node, new Path());
        }
        shortestPathsMap.put(beginNode, new Path());
        shortestPathsMap.put(endNode, new Path());

        distanceMap.put(beginNode, 0.0);
        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();
        unsettledNodes.add(beginNode);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes, distanceMap);
            unsettledNodes.remove(currentNode);
            for (Map.Entry<Node, Double> adjacencyPair:
                    currentNode.getAdjacentNodes().entrySet()) {
                Node adjacentNode = adjacencyPair.getKey();
                Double distance = adjacencyPair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode, distance, currentNode, distanceMap, shortestPathsMap);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        shortestPathsMap.get(endNode).addNode(endNode);
        return shortestPathsMap.get(endNode);
    }
}
