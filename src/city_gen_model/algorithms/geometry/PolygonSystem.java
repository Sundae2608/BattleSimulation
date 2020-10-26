package city_gen_model.algorithms.geometry;

import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.*;

public class PolygonSystem {

    // Object sets
    HashSet<Vertex> vertices;
    HashSet<Edge> edges;
    HashSet<Polygon> polygons;

    // Relational maps
    HashMap<Vertex, HashSet<Edge>> vertexToEdgeMap;
    HashMap<Vertex, HashSet<Polygon>> vertexToPolygonMap;
    HashMap<Edge, HashSet<Polygon>> edgeToPolygonMap;

    public PolygonSystem() {
        vertices = new HashSet<>();
        edges = new HashSet<>();
        polygons = new HashSet<>();

        vertexToEdgeMap = new HashMap<>();
        vertexToPolygonMap = new HashMap<>();
        edgeToPolygonMap = new HashMap<>();
    }

    /**
     * Construct a new polygon system based on the input polygons
     */
    public PolygonSystem(HashSet<Polygon> inputPolygons) {
        vertices = new HashSet<>();
        edges = new HashSet<>();
        polygons = new HashSet<>();

        vertexToEdgeMap = new HashMap<>();
        vertexToPolygonMap = new HashMap<>();
        edgeToPolygonMap = new HashMap<>();

        for (Polygon p : inputPolygons) {
            addPolygon(p);
        }
    }

    /**
     * Construct a new polygon system based on the input polygons
     */
    public PolygonSystem(List<Polygon> inputPolygons) {
        vertices = new HashSet<>();
        edges = new HashSet<>();
        polygons = new HashSet<>();

        vertexToEdgeMap = new HashMap<>();
        vertexToPolygonMap = new HashMap<>();
        edgeToPolygonMap = new HashMap<>();

        for (Polygon p : inputPolygons) {
            addPolygon(p);
        }
    }

    /**
     * Add a new polygon to the polygon system
     */
    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
        for (Vertex v : polygon.getVertices()) {
            if (!vertexToPolygonMap.containsKey(v)) {
                vertexToPolygonMap.put(v, new HashSet<>());
            }
            vertices.add(v);
            vertexToPolygonMap.get(v).add(polygon);
        }

        for (Edge edge : polygon.getEdges()) {
            if (!edgeToPolygonMap.containsKey(edge)) {
                edgeToPolygonMap.put(edge, new HashSet<>());
            }
            edges.add(edge);
            edgeToPolygonMap.get(edge).add(polygon);

            if (!vertexToEdgeMap.containsKey(edge.vertex1)) {
                vertexToEdgeMap.put(edge.vertex1, new HashSet<>());
            }
            vertexToEdgeMap.get(edge.vertex1).add(edge);

            if (!vertexToEdgeMap.containsKey(edge.vertex2)) {
                vertexToEdgeMap.put(edge.vertex2, new HashSet<>());
            }
            vertexToEdgeMap.get(edge.vertex2).add(edge);
        }
    }

    /**
     * Remove a polygon from the polygon system
     */
    public void removePolygon(Polygon polygon) {
        // Do nothing if the polygon is already not in the system.
        if (!polygons.contains(polygon)) {
            return;
        }

        // Remove the polygon, but only remove the vertices and the edges if the polygon represents the last vertex
        // and edge associated with that polygon.
        for (Vertex v : polygon.getVertices()) {
            if (vertexToPolygonMap.get(v) == null) continue;
            vertexToPolygonMap.get(v).remove(polygon);
            if (vertexToPolygonMap.get(v).size() == 0) {
                vertexToPolygonMap.remove(v);
                vertices.remove(v);
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
     * Move one of the vertex in the system.
     * Moving a vertex in the system can't simply be just changing position x and y alone but also changing all the
     * hashed position in each of the hashmap involving the changed vertices.
     */
    public void moveVertex(Vertex v, double newX, double newY) {

        // Remove all artifact of the current vertex from the current tracking HashMaps
        ArrayList<Polygon> affectedPolygonsThroughVertex = new ArrayList<>(vertexToPolygonMap.get(v));
        for (Polygon polygon : affectedPolygonsThroughVertex) {
            removePolygon(polygon);
        }

        // Change the vertex position
        v.setX(newX);
        v.setY(newY);

        // Re-add the polygons
        for (Polygon polygon : affectedPolygonsThroughVertex) {
            addPolygon(polygon);
        }
    }

    /**
     * Swap the edge of the triangle that shares edge.
     */
    public void swapTriangleEdge(Polygon p1, Polygon p2) {
        // Check to make sure both polygons are triangles
        if (p1.getVertices().size() != 3 || p2.getVertices().size() != 3) {
            return;
        }

        // Check eligibility. I order to swap edge, the two triangles must share exactly two points.
        HashSet<Vertex> sharedVertices = new HashSet<>();
        for (Vertex v1 : p1.getVertices()) {
            for (Vertex v2 : p2.getVertices()) {
                if (v1 == v2) sharedVertices.add(v1);
            }
        }
        if (sharedVertices.size() != 2) {
            // Two triangle must share exactly two points.
            return;
        }

        ArrayList<Vertex> separateVerticesArr = new ArrayList<>();
        for (Vertex v : p1.getVertices()) {
            if (!sharedVertices.contains(v)) separateVerticesArr.add(v);
        }
        for (Vertex v : p2.getVertices()) {
            if (!sharedVertices.contains(v)) separateVerticesArr.add(v);
        }
        ArrayList<Vertex> sharedVerticesArr = new ArrayList<>(sharedVertices);

        // Calculate the length of the shared vertices and separate vertices to have the correct scaling factors.
        double sharedVertexLen = MathUtils.quickDistance(
                sharedVerticesArr.get(0).getX(), sharedVerticesArr.get(0).getY(),
                sharedVerticesArr.get(1).getX(), sharedVerticesArr.get(1).getY());
        double separatedVertexLen = MathUtils.quickDistance(
                separateVerticesArr.get(0).getX(), separateVerticesArr.get(0).getY(),
                separateVerticesArr.get(1).getX(), separateVerticesArr.get(1).getY()
        );
        double scalingFactor = sharedVertexLen / separatedVertexLen;

        // Separate vertices will now be shared, while shared vertex will be distributed to each vertex.
        Vertex[] vertices1 = new Vertex[]{separateVerticesArr.get(0), separateVerticesArr.get(1), sharedVerticesArr.get(0)};
        HashSet<Edge> edges1 = new HashSet<>();
        edges1.add(new Edge(vertices1[0], vertices1[1]));
        edges1.add(new Edge(vertices1[0], vertices1[2]));
        edges1.add(new Edge(vertices1[1], vertices1[2]));
        Polygon newPolygon1 = new Polygon(new HashSet<>(Arrays.asList(vertices1)), edges1);

        Vertex[] vertices2 =  new Vertex[]{separateVerticesArr.get(0), separateVerticesArr.get(1), sharedVerticesArr.get(1)};
        HashSet<Edge> edges2 = new HashSet<>();
        edges2.add(new Edge(vertices2[0], vertices2[1]));
        edges2.add(new Edge(vertices2[0], vertices2[2]));
        edges2.add(new Edge(vertices2[1], vertices2[2]));
        Polygon newPolygon2 = new Polygon(new HashSet<>(Arrays.asList(vertices2)), edges2);

        // Check to make sure no newly separated resides within another triangles.
        if (PhysicUtils.checkPolygonPointCollision(
                new double[][]{
                        {vertices2[0].getX(), vertices2[0].getY()},
                        {vertices2[1].getX(), vertices2[1].getY()},
                        {vertices2[2].getX(), vertices2[2].getY()},
                }, sharedVerticesArr.get(0).getX(), sharedVerticesArr.get(0).getY())) {
            return;
        }
        if (PhysicUtils.checkPolygonPointCollision(
                new double[][]{
                        {vertices1[0].getX(), vertices1[0].getY()},
                        {vertices1[1].getX(), vertices1[1].getY()},
                        {vertices1[2].getX(), vertices1[2].getY()},
                }, sharedVerticesArr.get(1).getX(), sharedVerticesArr.get(1).getY())) {
            return;
        }

        // Remove old polygons from the system and create new polygons
        removePolygon(p1);
        removePolygon(p2);
        addPolygon(newPolygon1);
        addPolygon(newPolygon2);

        // The shared vertices need to get closer to each other
        double[] centerPt = new double[] {
                (separateVerticesArr.get(0).getX() + separateVerticesArr.get(1).getX()) / 2,
                (separateVerticesArr.get(0).getY() + separateVerticesArr.get(1).getY()) / 2,
        };
        double[] newPtVertex1 = MathUtils.scalePoint(centerPt, separateVerticesArr.get(0).getPt(), scalingFactor);
        double[] newPtVertex2 = MathUtils.scalePoint(centerPt, separateVerticesArr.get(1).getPt(), scalingFactor);
        moveVertex(separateVerticesArr.get(0), newPtVertex1[0], newPtVertex1[1]);
        moveVertex(separateVerticesArr.get(1), newPtVertex2[0], newPtVertex2[1]);
    }

    /**
     * Merge multiple polygons together
     */
    public Polygon mergeMultiplePolygons(List<Polygon> polygons) {
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

        // Get all the vertices from the edges
        HashSet<Vertex> vertices = new HashSet<>();
        for (Edge e : uniqueEdges) {
            vertices.add(e.getVertex1());
            vertices.add(e.getVertex2());
        }

        // Create the new polygon, remove old polygon and add this new polygon in
        Polygon newPolygon = new Polygon(vertices, uniqueEdges);
        for (Polygon polygon : polygons) {
            removePolygon(polygon);
        }
        addPolygon(newPolygon);

        // Return new polygon for external processing.
        return newPolygon;
    }

    /**
     * Merge multiple polygons together given a list of polygons.
     */
    public Polygon mergeMultiplePolygons(Set<Polygon> polygons) {
        List<Polygon> polygonList = new ArrayList<>();
        for (Polygon p : polygons) {
            polygonList.add(p);
        }
        return mergeMultiplePolygons(polygonList);
    }

    /**
     * Get all entities of the same type - note that these entities are after merge
     * Returning a list of the same entity type because we can have more than one city center, river, etc.
     * @param entityType
     */
    public List<Polygon> getEntities(EntityType entityType) {
        List<Polygon> entities = new ArrayList<>();
        for (Polygon polygon : getPolygons()) {
            if (polygon.getEntityType() == entityType) {
                entities.add(polygon);
            }
        }
        return entities;
    }

    /**
     * Return all polygons near the border of the map
     */
    public List<Polygon> getPolygonsNearTheEdge() {
        List<Polygon> polygonsNearEdge = new ArrayList<>();
        for (Polygon polygon : getPolygons()) {
            for (Edge edge : polygon.getEdges()) {
                if (edgeToPolygonMap.get(edge).size() == 1) {
                    polygonsNearEdge.add(polygon);
                    break;
                }
            }
        }
        return polygonsNearEdge;
    }

    /**
     * Create a polygon by connecting COM around vertex.
     * This functionality is fairly broken.
     */
    private Polygon createPolygonByConnectCOMAroundVertex(Vertex vertex) {
        HashSet<Polygon> polygonList = vertexToPolygonMap.get(vertex);

        // A map converting the angle to the COM
        HashMap<Double, double[]> angleToCom = new HashMap<>();
        for (Polygon polygon : polygonList) {
            double[] com = polygon.getCenterOfMass();
            double angle = Math.atan2(com[1] - vertex.getY(), com[0] - vertex.getX());
            angleToCom.put(angle, com);
        }

        // Sort the angles
        ArrayList<Double> angleList = new ArrayList<>(angleToCom.keySet());
        Collections.sort(angleList);

        int numAngles = angleList.size();
        HashSet<Edge> edges = new HashSet<>();
        HashSet<Vertex> vertices = new HashSet<>();
        double[] prevCom = angleToCom.get(angleList.get(0));
        Vertex prevVertex = new Vertex(prevCom[0], prevCom[1]);
        for (int i = 0; i < numAngles; i++) {
            double[] nextCom =  angleToCom.get(angleList.get((i + 1) % numAngles));
            // TODO: Don't create new vertex all the time. Instead used shared vertex and shared edge.
            Vertex nextVertex = new Vertex(nextCom[0], nextCom[1]);
            edges.add(new Edge(prevVertex, nextVertex));
            vertices.add(nextVertex);
            prevVertex = nextVertex;
        }
        return new Polygon(vertices, edges);
    }

    /**
     * Create intermediate polygons
     * TODO: Add picture documentation for something as opaque and hard to describe as this one.
     */
    public PolygonSystem createIntermediatePolygons() {
        PolygonSystem newSystem = new PolygonSystem();
        for (Vertex v : vertices) {
            if (vertexToPolygonMap.get(v) != null) {
                Polygon newPolygon = createPolygonByConnectCOMAroundVertex(v);
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

    public HashSet<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(HashSet<Vertex> vertices) {
        this.vertices = vertices;
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

    public HashSet<Polygon> getAdjacentPolygon(Vertex v) {
        return vertexToPolygonMap.get(v);
    }

    public HashSet<Edge> getAdjacentEdges(Vertex v) {
        return vertexToEdgeMap.get(v);
    }
}
