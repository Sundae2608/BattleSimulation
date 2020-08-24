package model.algorithms.geometry;

import model.algorithms.pathfinding.Node;
import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.*;

public class PolygonSystem {

    // Object sets
    HashSet<Node> nodes;
    HashSet<Edge> edges;
    HashSet<Polygon> polygons;

    // Relational maps
    HashMap<Node, HashSet<Edge>> nodeToEdgeMap;
    HashMap<Node, HashSet<Polygon>> nodeToPolygonMap;
    HashMap<Edge, HashSet<Polygon>> edgeToPolygonMap;

    public PolygonSystem() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        polygons = new HashSet<>();

        nodeToEdgeMap = new HashMap<>();
        nodeToPolygonMap = new HashMap<>();
        edgeToPolygonMap = new HashMap<>();
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
        for (Node node : polygon.getNodes()) {
            if (!nodeToPolygonMap.containsKey(node)) {
                nodeToPolygonMap.put(node, new HashSet<>());
            }
            nodes.add(node);
            nodeToPolygonMap.get(node).add(polygon);
        }

        for (Edge edge : polygon.getEdges()) {
            if (!edgeToPolygonMap.containsKey(edge)) {
                edgeToPolygonMap.put(edge, new HashSet<>());
            }
            edges.add(edge);
            edgeToPolygonMap.get(edge).add(polygon);

            if (!nodeToEdgeMap.containsKey(edge.node1)) {
                nodeToEdgeMap.put(edge.node1, new HashSet<>());
            }
            nodeToEdgeMap.get(edge.node1).add(edge);

            if (!nodeToEdgeMap.containsKey(edge.node2)) {
                nodeToEdgeMap.put(edge.node2, new HashSet<>());
            }
            nodeToEdgeMap.get(edge.node2).add(edge);
        }
    }

    public void removePolygon(Polygon polygon) {
        // Do nothing if the polygon is already not in the system.
        if (!polygons.contains(polygon)) {
            return;
        }

        // Remove the polygon, but only remove the nodes and the edges if the polygon represents the last node and edge
        // associated with that polygon.
        for (Node node : polygon.getNodes()) {
            if (nodeToPolygonMap.get(node) == null) continue;
            nodeToPolygonMap.get(node).remove(polygon);
            if (nodeToPolygonMap.get(node).size() == 0) {
                nodeToPolygonMap.remove(node);
                nodes.remove(node);
            }
        }
        for (Edge edge : polygon.getEdges()) {
            if (edgeToPolygonMap.get(edge) == null) continue;
            edgeToPolygonMap.get(edge).remove(polygon);
            if (edgeToPolygonMap.get(edge).size() == 0) {
                edgeToPolygonMap.remove(edge);
                edges.remove(edge);
            }
        }
        polygons.remove(polygon);
    }

    /**
     * Move one of the node in the system.
     * Moving a node in the system can't simply be just changing position x and y alone but also changing all the
     * hashed position in each of the hashmap involving the changed nodes.
     */
    public void moveNode(Node node, double newX, double newY) {

        /** Remove all artifact of the current node from the current tracking HashMaps */
        // Remove the nodes and store the polygons
        ArrayList<Polygon> affectedPolygonsThroughNode = new ArrayList<>(nodeToPolygonMap.get(node));
        for (Polygon polygon : affectedPolygonsThroughNode) {
            removePolygon(polygon);
        }

        /** Change the node position */
        node.setX(newX);
        node.setY(newY);

        /** Re add the polygons */
        for (Polygon polygon : affectedPolygonsThroughNode) {
            addPolygon(polygon);
        }
    }

    /**
     * Swap the edge of the triangle that shares edge.
     */
    public void swapTriangleEdge(Polygon p1, Polygon p2) {
        // Check to make sure both polygons are triangles
        if (p1.getNodes().size() != 3 || p2.getNodes().size() != 3) {
            return;
        }

        // Check eligibility. I order to swap edge, the two triangles must share exactly two points.
        HashSet<Node> sharedNodes = new HashSet<>();
        for (Node node1 : p1.getNodes()) {
            for (Node node2 : p2.getNodes()) {
                if (node1 == node2) sharedNodes.add(node1);
            }
        }
        if (sharedNodes.size() != 2) {
            // Two triangle must share exactly two points.
            return;
        }

        ArrayList<Node> separateNodesArr = new ArrayList<>();
        for (Node node : p1.getNodes()) {
            if (!sharedNodes.contains(node)) separateNodesArr.add(node);
        }
        for (Node node : p2.getNodes()) {
            if (!sharedNodes.contains(node)) separateNodesArr.add(node);
        }
        ArrayList<Node> sharedNodesArr = new ArrayList<>(sharedNodes);

        // Calculate the length of the shared nodes and separate nodes to have the correct scaling factors.
        double sharedNodeLen = MathUtils.quickDistance(
                sharedNodesArr.get(0).getX(), sharedNodesArr.get(0).getY(),
                sharedNodesArr.get(1).getX(), sharedNodesArr.get(1).getY());
        double separatedNodeLen = MathUtils.quickDistance(
                separateNodesArr.get(0).getX(), separateNodesArr.get(0).getY(),
                separateNodesArr.get(1).getX(), separateNodesArr.get(1).getY()
        );
        double scalingFactor = sharedNodeLen / separatedNodeLen;

        // Separate nodes will now be shared, while shared node will be distributed to each node.
        Node[] nodes1 = new Node[]{separateNodesArr.get(0), separateNodesArr.get(1), sharedNodesArr.get(0)};
        HashSet<Edge> edges1 = new HashSet<>();
        edges1.add(new Edge(nodes1[0], nodes1[1]));
        edges1.add(new Edge(nodes1[0], nodes1[2]));
        edges1.add(new Edge(nodes1[1], nodes1[2]));
        Polygon newPolygon1 = new Polygon(new HashSet<>(Arrays.asList(nodes1)), edges1);

        Node[] nodes2 =  new Node[]{separateNodesArr.get(0), separateNodesArr.get(1), sharedNodesArr.get(1)};
        HashSet<Edge> edges2 = new HashSet<>();
        edges2.add(new Edge(nodes2[0], nodes2[1]));
        edges2.add(new Edge(nodes2[0], nodes2[2]));
        edges2.add(new Edge(nodes2[1], nodes2[2]));
        Polygon newPolygon2 = new Polygon(new HashSet<>(Arrays.asList(nodes2)), edges2);

        // Check to make sure no newly separated resides within another triangles.
        if (PhysicUtils.checkPolygonPointCollision(
            new double[][]{
                {nodes2[0].getX(), nodes2[0].getY()},
                {nodes2[1].getX(), nodes2[1].getY()},
                {nodes2[2].getX(), nodes2[2].getY()},
            }, sharedNodesArr.get(0).getX(), sharedNodesArr.get(0).getY())) {
            return;
        }
        if (PhysicUtils.checkPolygonPointCollision(
            new double[][]{
                {nodes1[0].getX(), nodes1[0].getY()},
                {nodes1[1].getX(), nodes1[1].getY()},
                {nodes1[2].getX(), nodes1[2].getY()},
            }, sharedNodesArr.get(1).getX(), sharedNodesArr.get(1).getY())) {
            return;
        }

        // Remove old polygons from the system and create new polygons
        removePolygon(p1);
        removePolygon(p2);
        addPolygon(newPolygon1);
        addPolygon(newPolygon2);

        // The shared nodes need to get closer to each other
        double[] centerPt = new double[] {
                (separateNodesArr.get(0).getX() + separateNodesArr.get(1).getX()) / 2,
                (separateNodesArr.get(0).getY() + separateNodesArr.get(1).getY()) / 2,
        };
        double[] newPtNode1 = MathUtils.scalePoint(centerPt, separateNodesArr.get(0).getPt(), scalingFactor);
        double[] newPtNode2 = MathUtils.scalePoint(centerPt, separateNodesArr.get(1).getPt(), scalingFactor);
        moveNode(separateNodesArr.get(0), newPtNode1[0], newPtNode1[1]);
        moveNode(separateNodesArr.get(1), newPtNode2[0], newPtNode2[1]);
    }

    /**
     * Merge multiple polygons together
     */
    public Polygon mergeMultiplePolygons(ArrayList<Polygon> polygons) {
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

        // Create the new polygon, remove old polygon and add this new polygon in
        Polygon newPolygon = new Polygon(nodes, uniqueEdges);
        for (Polygon polygon : polygons) {
            removePolygon(polygon);
        }
        addPolygon(newPolygon);

        // Return new polygon for external processing.
        return newPolygon;
    }

    /**
     * Create a polygon by connecting COM around node.
     */
    private Polygon createPolygonByConnectCOMAroundNode(Node node) {
        HashSet<Polygon> polygonList = nodeToPolygonMap.get(node);

        // A map converting the angle to the COM
        HashMap<Double, double[]> angleToCom = new HashMap<>();
        for (Polygon polygon : polygonList) {
            double[] com = polygon.getCenterOfMass();
            double angle = Math.atan2(com[1] - node.getY(), com[0] - node.getX());
            angleToCom.put(angle, com);
        }

        // Sort the angles
        ArrayList<Double> angleList = new ArrayList<>(angleToCom.keySet());
        Collections.sort(angleList);

        int numAngles = angleList.size();
        HashSet<Edge> edges = new HashSet<>();
        HashSet<Node> nodes = new HashSet<>();
        double[] prevCom = angleToCom.get(angleList.get(0));
        Node prevNode = new Node(prevCom[0], prevCom[1]);
        for (int i = 0; i < numAngles; i++) {
            double[] nextCom =  angleToCom.get(angleList.get((i + 1) % numAngles));
            Node nextNode = new Node(nextCom[0], nextCom[1]);
            edges.add(new Edge(prevNode, nextNode));
            nodes.add(nextNode);
            prevNode = nextNode;
        }
        return new Polygon(nodes, edges);
    }

    /**
     * Create intermediate polygons
     * TODO: Add picture documentation for something as opague and hard to describe as this one.
     */
    public PolygonSystem createIntermediatePolygons() {
        PolygonSystem newSystem = new PolygonSystem();
        for (Node node : nodes) {
            if (nodeToPolygonMap.get(node) != null) {
                Polygon newPolygon = createPolygonByConnectCOMAroundNode(node);
                if (newPolygon.getEdges().size() >= 3) {
                    newSystem.addPolygon(newPolygon);
                }
            }
        }
        return newSystem;
    }

    public HashSet<Polygon> getPolygons() {
        return polygons;
    }

    public HashSet<Node> getNodes() {
        return nodes;
    }

    public void setNodes(HashSet<Node> nodes) {
        this.nodes = nodes;
    }

    public HashSet<Edge> getEdges() {
        return edges;
    }

    public void setEdges(HashSet<Edge> edges) {
        this.edges = edges;
    }

    public HashSet<Polygon> getAdjacentPolygon(Edge e) {
        return edgeToPolygonMap.get(e);
    }

    public HashSet<Polygon> getAdjacentPolygon(Node n) {
        return nodeToPolygonMap.get(n);
    }
}
