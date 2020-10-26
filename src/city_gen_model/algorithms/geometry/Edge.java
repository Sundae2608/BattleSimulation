package city_gen_model.algorithms.geometry;

import model.utils.MathUtils;

public class Edge {

    // Each edge contains two nodes
    Vertex vertex1;
    Vertex vertex2;

    public Edge(Vertex inputNode1, Vertex inputNode2) {
        vertex1 = inputNode1;
        vertex2 = inputNode2;
    }

    @Override
    public boolean equals(Object o)
    {
        // Check the memory of the object before checking the object's content.
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Edge edge = (Edge) o;

        // Two edges are equal if they have the same nodes.
        return (vertex1 == edge.vertex1 && vertex2 == edge.vertex2) || (vertex1 == edge.vertex2 && vertex2 == edge.vertex1);
    }

    @Override
    public int hashCode()
    {
		int result = vertex1.hashCode() ^ vertex2.hashCode();
		return result;
    }

    public double getLength() {
        return MathUtils.distance(vertex1.getX(), vertex1.getY(), vertex2.getX(), vertex2.getY());
    }

    public Vertex getVertex1() {
        return vertex1;
    }

    public Vertex getVertex2() {
        return vertex2;
    }
}
