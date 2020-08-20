package model.algorithms.pathfinding;

public class Triangle {

    // Each triangle contains 3 nodes.
    Node node1;
    Node node2;
    Node node3;

    public Triangle(Node inputNode1, Node inputNode2, Node inputNode3) {
        node1 = inputNode1;
        node2 = inputNode2;
        node3 = inputNode3;
    }

    public Node[] getNodes() {
        return new Node[] {node1, node2, node3};
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    public Node getNode3() {
        return node3;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }

    public void setNode3(Node node3) {
        this.node3 = node3;
    }

    public double[] getCenterOfMass() {
        return new double[] {
                (node1.getX() + node2.getX() + node3.getX()) / 3,
                (node1.getY() + node2.getY() + node3.getY()) / 3
        };
    }

    public double[][] getPoints() {
        return new double[][] {
                {node1.getX(), node1.getY()},
                {node2.getX(), node2.getY()},
                {node3.getX(), node3.getY()}
        };
    }
}
