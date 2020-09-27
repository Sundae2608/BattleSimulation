package model.algorithms;

import model.construct.Construct;
import model.projectile_objects.BaseObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ConstructHasher {
    // Height and width of each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<Construct> constructs;

    // Hash view.map containing
    private HashMap<Long, ArrayList<BaseObject>> hashMap;

    public ConstructHasher(int xDivision, int yDivision, ArrayList<Construct> inputConstructs) {
        xDiv = xDivision;
        yDiv = yDivision;
        constructs = inputConstructs;
        hashMap = new HashMap<>();

        // TODO: Hash all constructs. Construct only needs to be hashed once.
        //  Do this when there is a lot of objects. Currently, always doing the check is not too expensive.
    }

    /**
     * Return the list of potential collision candidates based on position (x, y).
     */
    public ArrayList<Construct> getCandidateConstructs(double x, double y) {
        return constructs;
    }

    /**
     * Convert xHash and yHash into a single unique hash key
     * The hash key is essentially a long integer with the left side xHash and the right side yHash
     */
    private long pairHash(int xHash, int yHash) {
        return ((long)xHash << 32) | (yHash & 0XFFFFFFFFL);
    }
}
