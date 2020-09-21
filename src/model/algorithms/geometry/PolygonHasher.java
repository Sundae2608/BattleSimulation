package model.algorithms.geometry;

import it.unimi.dsi.fastutil.bytes.ByteHash;
import model.objects.BaseObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PolygonHasher {
    // Height of width each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<Polygon> polygons;

    // Hash map connecting the hash code of a block to the houses
    private HashMap<Long, ArrayList<Polygon>> hashMap;

    public PolygonHasher(int xDivision, int yDivision) {
        xDiv = xDivision;
        yDiv = yDivision;
        polygons = new ArrayList<>();
        hashMap = new HashMap<>();
    }

    public void addObject(Polygon p) {
        int minXHash = Integer.MAX_VALUE;
        int maxXHash = Integer.MIN_VALUE;
        int minYHash = Integer.MAX_VALUE;
        int maxYHash = Integer.MIN_VALUE;
        for (Vertex v : p.getVertices()) {
            int xHash = (int) (v.x / xDiv);
            int yHash = (int) (v.y / yDiv);
            if (xHash < minXHash) {
                minXHash = xHash;
            }
            if (xHash > maxXHash) {
                maxXHash = xHash;
            }
            if (yHash < minYHash) {
                minYHash = yHash;
            }
            if (yHash > maxYHash) {
                maxYHash = yHash;
            }
        }
        for (int i = minXHash; i <= maxXHash; i++) {
            for (int j = minYHash; j <= maxYHash; j++) {
                long key = pairHash(i, j);
                if (!hashMap.containsKey(key)) {
                    hashMap.put(key, new ArrayList<>());
                }
                hashMap.get(key).add(p);
            }
        }
        polygons.add(p);
    }

    /**
     * Return the list of potential collision of a circle at (x, y) with radius r.
     */
    public ArrayList<Polygon> getCollisionObjects(double x, double y, double r) {
        int xHash1 = (int) ((x - r) / xDiv);
        int xHash2 = (int) ((x + r) / xDiv);

        int yHash1 = (int) ((y - r) / yDiv);
        int yHash2 = (int) ((y + r) / yDiv);
        ArrayList<Polygon> collideList = new ArrayList<>();
        for (int i = xHash1; i <= xHash2; i++) {
            for (int j = yHash1; j <= yHash2; j++) {
                long key = pairHash(i, j);
                if (!hashMap.containsKey(key)) continue;
                for (Polygon otherPolygon : hashMap.get(key)) {
                    collideList.add(otherPolygon);
                }
            }
        }
        return collideList;
    }

    /**
     * Return the list of potential collision candidates of a polygon
     */
    public ArrayList<Polygon> getCollisionObjects(Polygon p) {
        // Min xHash, max xHash, min yHash, max yHash
        int minXHash = Integer.MAX_VALUE;
        int maxXHash = Integer.MIN_VALUE;
        int minYHash = Integer.MAX_VALUE;
        int maxYHash = Integer.MIN_VALUE;
        for (Vertex v : p.getVertices()) {
            int xHash = (int) (v.x / xDiv);
            int yHash = (int) (v.y / yDiv);
            if (xHash < minXHash) {
                minXHash = xHash;
            }
            if (xHash > maxXHash) {
                maxXHash = xHash;
            }
            if (yHash < minYHash) {
                minYHash = yHash;
            }
            if (yHash > maxYHash) {
                maxYHash = yHash;
            }
        }
        ArrayList<Polygon> collideList = new ArrayList<>();
        for (int i = minXHash; i <= maxXHash; i++) {
            for (int j = minYHash; j <= maxYHash; j++) {
                long key = pairHash(i, j);
                if (!hashMap.containsKey(key)) continue;
                for (Polygon otherPolygon : hashMap.get(key)) {
                    collideList.add(otherPolygon);
                }
            }
        }
        return collideList;
    }

    /**
     * Convert xHash and yHash into a single unique hash key
     * The hashkey is essentially a long integer with the left side xHash and the right side yHash
     */
    private long pairHash(int xHash, int yHash) {
        return ((long) xHash << 32) | (yHash & 0XFFFFFFFFL);
    }
}
