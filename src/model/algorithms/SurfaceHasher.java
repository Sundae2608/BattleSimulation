package model.algorithms;

import model.enums.SurfaceType;
import model.surface.BaseSurface;
import model.surface.ForestSurface;
import model.surface.Tree;
import model.utils.PhysicUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SurfaceHasher {
    // Height and width of each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<BaseSurface> surfaces;
    private ArrayList<Tree> trees;

    // Hash map containing trees
    private HashMap<Long, ArrayList<Tree>> treeHashMap;
    private HashMap<Long, ArrayList<BaseSurface>> surfaceHashMap;

    public SurfaceHasher(int xDivision, int yDivision, ArrayList<BaseSurface> inputSurfaces) {
        xDiv = xDivision;
        yDiv = yDivision;
        surfaces = inputSurfaces;
        trees = new ArrayList<>();
        treeHashMap = new HashMap<>();
        surfaceHashMap = new HashMap<>();

        // Add surface to surface hash map
        for (BaseSurface surface : surfaces) {
            int minXHash = Integer.MAX_VALUE;
            int maxXHash = Integer.MIN_VALUE;
            int minYHash = Integer.MAX_VALUE;
            int maxYHash = Integer.MIN_VALUE;
            for (double[] pt : surface.getSurfaceBoundary()) {
                int xHash = (int) pt[0] / xDiv;
                int yHash = (int) pt[1] / yDiv;
                if (xHash > maxXHash) {
                    maxXHash = xHash;
                }
                if (xHash < minXHash) {
                    minXHash = xHash;
                }
                if (yHash > minYHash) {
                    maxYHash = yHash;
                }
                if (yHash < minYHash) {
                    minYHash = yHash;
                }
            }

            for (int i = minXHash; i < maxXHash; i++) {
                for (int j = minYHash; j < maxYHash; j++) {
                    double x = i * xDiv + xDiv / 2;
                    double y = j * yDiv + yDiv / 2;
                    if (PhysicUtils.checkPolygonPointCollision(surface.getSurfaceBoundary(), x, y)) {
                        long hash = pairHash(i, j);
                        if (!surfaceHashMap.containsKey(hash)) treeHashMap.put(hash, new ArrayList<>());
                        surfaceHashMap.get(hash).add(surface);
                    }
                }
            }

            // It it is a forest, we add the trees in
            if (surface.getType() == SurfaceType.FOREST) {
                for (Tree tree : ((ForestSurface) surface).getTrees()) {
                    trees.add(tree);
                }
            }
        }

        // Add tree object to tree hash map
        for (Tree tree : trees) {
            int xHash = (int)tree.getX() / xDiv;
            int yHash = (int)tree.getY() / yDiv;
            long hash = pairHash(xHash, yHash);
            if (!treeHashMap.containsKey(hash)) treeHashMap.put(hash, new ArrayList<>());
            treeHashMap.get(hash).add(tree);
        }
    }

    /**
     * Return the list of potential surface candidates based on position (x, y).
     */
    public ArrayList<BaseSurface> getCandidateSurfaces(double x, double y) {
        int xHash = (int) x / xDiv;
        int yHash = (int) y / yDiv;
        long hash = pairHash(xHash, yHash);
        return surfaceHashMap.get(hash);
    }

    /**
     * Return the list of potential tree candidates based on position (x, y).
     * @return
     */
    public ArrayList<Tree> getCandidateTrees(double x, double y) {
        ArrayList<Tree> returnTrees = new ArrayList<>();
        int xHash = (int) x / xDiv;
        int yHash = (int) y / yDiv;
        for (int i = xHash - 1; i < xHash + 2; i++) {
            for (int j = yHash - 1; j < yHash + 2; j++) {
                long hash = pairHash(i, j);
                if (!treeHashMap.containsKey(hash)) continue;
                for (Tree tree : treeHashMap.get(hash)) {
                    returnTrees.add(tree);
                }
            }
        }
        return returnTrees;
    }

    /**
     * Convert xHash and yHash into a single unique hash key
     * The hash key is essentially a long integer with the left side xHash and the right side yHash
     */
    private long pairHash(int xHash, int yHash) {
        return ((long)xHash << 32) | (yHash & 0XFFFFFFFFL);
    }
}
