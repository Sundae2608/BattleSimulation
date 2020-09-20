package model.algorithms;

import javafx.util.Pair;
import model.surface.Tree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static model.utils.PhysicUtils.checkPointCircleCollision;

public class TreeHasher {
    // Height and width of each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<model.surface.Tree> objects;

    // Hash map containing
    private HashMap<Long, ArrayList<Tree>> hashMap;

    public TreeHasher(int xDivision, int yDivision) {
        xDiv = xDivision;
        yDiv = yDivision;
        objects = new ArrayList<>();
        hashMap = new HashMap<>();
    }

    /**
     * Add a Tree objects to the space hasher
     */
    public void addObject(Tree obj) {objects.add(obj);
    }

    /**
     * Add an array of Tree objects to the space hasher
     */
    public void addObjectArray(ArrayList<Tree> TreeArrayList) {
        for (Tree obj : TreeArrayList) {
            objects.add(obj);
        }
    }

        /**
     * Hash all objects into the internal hashMap of the space hasher. Each object will be appended to an array list at
     * a certain key based on its position.
     */
    public void hashObjects() {
        // Clear the hashmap
        hashMap.clear();

        // Add objects into hashmap with key according to their position
        ArrayList<Tree> newObjects = new ArrayList<>();

        for (Tree obj : objects) {
            ArrayList<Integer> xHashes = new ArrayList<Integer>();
            ArrayList<Integer>  yHashes = new ArrayList<Integer>();

            // Assign Tree object to correct position
            double X = obj.getX();
            double Y = obj.getY();
            double radius = obj.getRadius();

            // Checking whether a tree is in multiple squares at the same time.
            // The maximum number of squares that a tree can be in is 4.
            // Here, we consider a tree to be inside a square.
            int xHash1 = (int) ((X - radius)/xDiv);
            int xHash2 = (int) ((X + radius)/xDiv);

            int yHash1 = (int) ((Y - radius)/yDiv);
            int yHash2 = (int) ((Y + radius)/yDiv);

            HashSet<Pair<Integer, Integer>> hashPts = new HashSet<>();

            for (int i = xHash1; i <= xHash2; i++) {
                for (int j = yHash1; j <= yHash2; j++) {
                    hashPts.add(new Pair<>(i, j));
                }
            }

            for (Pair<Integer, Integer> pair : hashPts){
                long key = pairHash(pair.getKey(), pair.getValue());
                if (!hashMap.containsKey(key)) {
                    hashMap.put(key, new ArrayList<>());
                }
                hashMap.get(key).add(obj);
            }

            // Add to new list of objects. This is so that new object list has all the dead troops cleared out of the
            // collision field
            newObjects.add(obj);
        }
        objects = newObjects;
    }

    /**
     * Convert xHash and yHash into a single unique hash key
     * The hashkey is essentially a long integer with the left side xHash and the right side yHash
     */
    private long pairHash(int xHash, int yHash) {
        return ((long)xHash << 32) | (yHash & 0XFFFFFFFFL);
    }

    /**
     * This function returns all the trees in the main square cell as well as all trees in the neighboring 8 cells
     * @param x
     * @param y
     * @return
     */
    public ArrayList<Tree> getTreesByCoordinate (double x, double y) {
        // Assign Tree object to correct position
        int xHash = (int) x / xDiv;
        int yHash = (int) y / yDiv;

        ArrayList<Tree> trees = new ArrayList<>();
        long key = pairHash((int) xHash, (int) yHash);
        if (hashMap.get(key) != null) {
            trees.addAll(hashMap.get(key));
        }

        return trees;
    }

    /**
     * Since a tree has radius, we want to return a tree if we stay in its radius
     * @param x
     * @param y
     * @return
     */
    public Tree getSingleTreeByCoordinate (double x, double y) {
        Tree returnTree = null;

        // Assign Tree object to correct position
        ArrayList<Tree> trees = getTreesByCoordinate (x, y);
        for (Tree tree : trees) {
            if (checkPointCircleCollision(x, y, tree.getX(), tree.getY(), tree.getRadius())) {
                returnTree = tree;
            }
        }
        return returnTree;
    }


    /**
     * Getter and setters
     */
    public ArrayList<Tree> getObjects() {
        return objects;
    }
}

