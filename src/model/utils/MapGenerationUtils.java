package model.utils;

import model.algorithms.pathfinding.Node;
import model.algorithms.pathfinding.Triangle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class MapGenerationUtils {

    public static void swapTriangleEdges(Triangle t1, Triangle t2, HashMap<Node, HashSet<Triangle>> nodeToTriangleMap) {

        // Remove current adjacency
        nodeToTriangleMap.get(t1.getNode1()).remove(t1);
        nodeToTriangleMap.get(t1.getNode2()).remove(t1);
        nodeToTriangleMap.get(t1.getNode3()).remove(t1);

        nodeToTriangleMap.get(t2.getNode1()).remove(t2);
        nodeToTriangleMap.get(t2.getNode2()).remove(t2);
        nodeToTriangleMap.get(t2.getNode3()).remove(t2);

        // Check eligibility. I order to swap edge, the two triangles must share exactly two points.
        int nodeShared = 0;
        HashSet<Node> sharedNodes = new HashSet<>();
        if (t1.getNode1() == t2.getNode1()) { nodeShared++; sharedNodes.add(t1.getNode1()); }
        if (t1.getNode1() == t2.getNode2()) { nodeShared++; sharedNodes.add(t1.getNode1()); }
        if (t1.getNode1() == t2.getNode3()) { nodeShared++; sharedNodes.add(t1.getNode1()); }

        if (t1.getNode2() == t2.getNode1()) { nodeShared++; sharedNodes.add(t1.getNode2()); }
        if (t1.getNode2() == t2.getNode2()) { nodeShared++; sharedNodes.add(t1.getNode2()); }
        if (t1.getNode2() == t2.getNode3()) { nodeShared++; sharedNodes.add(t1.getNode2()); }

        if (t1.getNode3() == t2.getNode1()) { nodeShared++; sharedNodes.add(t1.getNode3()); }
        if (t1.getNode3() == t2.getNode2()) { nodeShared++; sharedNodes.add(t1.getNode3()); }
        if (t1.getNode3() == t2.getNode3()) { nodeShared++; sharedNodes.add(t1.getNode3()); }
        if (nodeShared != 2) {
            // Two triangle must share exactly two points.
            return;
        }
        ArrayList<Node> separateNodesArr = new ArrayList<>();
        for (Node node : t1.getNodes()) {
            if (!sharedNodes.contains(node)) separateNodesArr.add(node);
        }
        for (Node node : t2.getNodes()) {
            if (!sharedNodes.contains(node)) separateNodesArr.add(node);
        }
        ArrayList<Node> sharedNodesArr = new ArrayList<>(sharedNodes);

        // Separate nodes will now be shared, while shared node will be distributed to each node.
        t1.setNode1(separateNodesArr.get(0));
        t2.setNode1(separateNodesArr.get(0));
        t1.setNode2(separateNodesArr.get(1));
        t2.setNode2(separateNodesArr.get(1));
        t1.setNode3(sharedNodesArr.get(0));
        t2.setNode3(sharedNodesArr.get(1));

        // Add new adjacency
        nodeToTriangleMap.get(separateNodesArr.get(0)).add(t1);
        nodeToTriangleMap.get(separateNodesArr.get(0)).add(t2);
        nodeToTriangleMap.get(separateNodesArr.get(1)).add(t1);
        nodeToTriangleMap.get(separateNodesArr.get(1)).add(t2);
        nodeToTriangleMap.get(sharedNodesArr.get(0)).add(t1);
        nodeToTriangleMap.get(sharedNodesArr.get(1)).add(t2);

        // The shared nodes need to get closer to each other
        double[] centerPt = new double[] {
                (separateNodesArr.get(0).getX() + separateNodesArr.get(1).getX()) / 2,
                (separateNodesArr.get(0).getY() + separateNodesArr.get(1).getY()) / 2,
        };
        double[] newPtNode1 = MathUtils.scalePoint(centerPt, separateNodesArr.get(0).getPt(), 0.5);
        double[] newPtNode2 = MathUtils.scalePoint(centerPt, separateNodesArr.get(1).getPt(), 0.5);
        separateNodesArr.get(0).setX(newPtNode1[0]);
        separateNodesArr.get(0).setY(newPtNode1[1]);
        separateNodesArr.get(1).setX(newPtNode2[0]);
        separateNodesArr.get(1).setY(newPtNode2[1]);
    }
}
