package model.algorithms;

import model.enums.SurfaceType;
import model.surface.BaseSurface;
import model.surface.ForestSurface;
import model.surface.Tree;

import java.util.ArrayList;
import java.util.HashMap;

public class SurfaceHasher {
    // Height and width of each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<BaseSurface> surfaces;
    private ArrayList<Tree> trees;

    // Hash view.map containing
    private HashMap<Long, ArrayList<Tree>> hashMap;

    public SurfaceHasher(int xDivision, int yDivision, ArrayList<BaseSurface> inputSurfaces) {
        xDiv = xDivision;
        yDiv = yDivision;
        surfaces = inputSurfaces;
        trees = new ArrayList<>();
        for (BaseSurface surface : surfaces) {
            if (surface.getType() == SurfaceType.FOREST) {
                for (Tree tree : ((ForestSurface) surface).getTrees()) {
                    trees.add(tree);
                }
            }
        }
        hashMap = new HashMap<>();

        // TODO: Hash all surfaces. Construct only needs to be hashed once.
        //  Do this when there is a lot of objects. Currently, always doing the check is not too expensive.
        for (Tree tree : trees) {
            int xHash = (int)tree.getX() / xDiv;
            int yHash = (int)tree.getY() / yDiv;
            long hash = pairHash(xHash, yHash);
            if (!hashMap.containsKey(hash)) hashMap.put(hash, new ArrayList<>());
            hashMap.get(hash).add(tree);
        }
    }

    /**
     * Return the list of potential surface candidates based on position (x, y).
     */
    public ArrayList<BaseSurface> getCandidateSurfaces(double x, double y) {
        return surfaces;
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
                if (!hashMap.containsKey(hash)) continue;
                for (Tree tree : hashMap.get(hash)) {
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
