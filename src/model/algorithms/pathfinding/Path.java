package model.algorithms.pathfinding;

import java.util.LinkedList;

public class Path {

    LinkedList<Node> nodes;

    public Path(Path path) {
        nodes = new LinkedList<>();
        for (Node node : path.getNodes()) {
            nodes.add(node);
        }
    }
    public Path() {
        nodes = new LinkedList<>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }
}
