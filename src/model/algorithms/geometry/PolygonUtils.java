package model.algorithms.geometry;

import java.util.*;

public class PolygonUtils {
    /**
     * Use BFS to the shortest path from one polygon to another polygon.
     * We use this search algorithm to construct a list of polygons that would make a river.
     * TODO: Rename the river object.
     * @param riverBegin The beginning polygon.
     * @param riverEnd The ending polygon.
     * @return A list of polygon (not necessary in order) that constructs the path from beginning to the end.
     */
    public static List<Polygon> findRiverPathBFS(PolygonSystem polygonSystem, Polygon riverBegin, Polygon riverEnd) {
        HashSet<Polygon> visited = new HashSet<>();
        Queue<List<Polygon>> pathQueue = new LinkedList<>();
        List<Polygon> path = new ArrayList<>();
        path.add(riverBegin);
        pathQueue.add(path);
        visited.add(riverBegin);

        while (!pathQueue.isEmpty()) {
            List<Polygon> currentPath = pathQueue.poll();
            Polygon currentPathEnd = currentPath.get(currentPath.size()-1);

            if (currentPathEnd == riverEnd) {
                return currentPath;
            }

            for (Edge currentEdge : currentPathEnd.getEdges()) {
                for (Polygon nextPolygon : polygonSystem.getAdjacentPolygon(currentEdge)) {
                    if (!visited.contains(nextPolygon) && nextPolygon.getEntityType() == EntityType.DEFAULT) {
                        visited.add(nextPolygon);
                        List<Polygon> nextPath = new ArrayList<>(currentPath);
                        nextPath.add(nextPolygon);
                        pathQueue.add(nextPath);
                    }
                }
            }
        }
        return path;
    }
}
