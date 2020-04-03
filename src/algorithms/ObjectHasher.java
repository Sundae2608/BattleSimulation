package algorithms;

import singles.BaseObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectHasher {
    // Height and width of each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<BaseObject> objects;

    // Hash map containing
    private HashMap<Long, ArrayList<BaseObject>> hashMap;

    public ObjectHasher(int xDivision, int yDivision) {
        xDiv = xDivision;
        yDiv = yDivision;
        objects = new ArrayList<>();
        hashMap = new HashMap<>();
    }

    /**
     * Add a BaseSingle objects to the space hasher
     */
    public void addObject(BaseObject obj) {
        objects.add(obj);
    }

    /**
     * Return the list of potential collision candidates of a BaseSingle obj
     */
    public ArrayList<BaseObject> getCollisionObjects(BaseObject obj) {
        int xHash = (int)obj.getX() / xDiv;
        int yHash = (int)obj.getY() / yDiv;

        ArrayList<BaseObject> collideList = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                long key = pairHash(xHash + i, yHash + j);
                if (!hashMap.containsKey(key)) continue;
                for (BaseObject otherObj : hashMap.get(key)) {
                    if (otherObj != obj) collideList.add(otherObj);
                }
            }
        }
        return collideList;
    }

    /**
     * Hash all objects into the internal hashMap of the space hasher. Each object will be appended to an array list at
     * a certain key based on its position.
     */
    public void hashObjects() {
        // Clear the hashmap
        hashMap.clear();

        // Add objects into hashmap with key according to their position
        ArrayList<BaseObject> newObjects = new ArrayList<>();
        for (BaseObject obj : objects) {

            // Check if the object should still be alive in this turn
            if (!obj.isAlive()) continue;

            // Assign alive object to correct position
            int xHash = (int)obj.getX() / xDiv;
            int yHash = (int)obj.getY() / yDiv;
            long key = pairHash(xHash, yHash);
            if (!hashMap.containsKey(key)) {
                hashMap.put(key, new ArrayList<>());
            }
            hashMap.get(key).add(obj);

            // Add to new list of objects. This is so that new object list has all the dead troops cleared out of the
            // collision field
            newObjects.add(obj);
        }
        objects = newObjects;
    }

    /**
     * Update objects
     */
    public void updateObjects() {
        // Update the states of all objects in the object hasher.
        for (BaseObject obj : objects) {
            obj.update();
        }
    }

    /**
     * Convert xHash and yHash into a single unique hash key
     * The hashkey is essentially a long integer with the left side xHash and the right side yHash
     */
    private long pairHash(int xHash, int yHash) {
        return ((long)xHash << 32) | (yHash & 0XFFFFFFFFL);
    }

    /**
     * Getter and setters
     */
    public ArrayList<BaseObject> getObjects() {
        return objects;
    }
}
