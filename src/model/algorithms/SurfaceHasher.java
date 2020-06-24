package model.algorithms;

import model.construct.Construct;
import model.objects.BaseObject;
import model.surface.BaseSurface;

import java.util.ArrayList;
import java.util.HashMap;

public class SurfaceHasher {
    // Height and width of each space (called xDiv and yDiv)
    private int xDiv;
    private int yDiv;
    private ArrayList<BaseSurface> surfaces;

    // Hash view.map containing
    private HashMap<Long, ArrayList<BaseObject>> hashMap;

    public SurfaceHasher(int xDivision, int yDivision, ArrayList<BaseSurface> inputSurfaces) {
        xDiv = xDivision;
        yDiv = yDivision;
        surfaces = inputSurfaces;
        hashMap = new HashMap<>();

        // TODO: Hash all surfaces. Construct only needs to be hashed once.
        //  Do this when there is a lot of objects. Currently, always doing the check is not too expensive.
    }

    /**
     * Return the list of potential surface candidates based on position (x, y).
     */
    public ArrayList<BaseSurface> getCandidateSurfaces(double x, double y) {
        return surfaces;
    }

    /**
     * Convert xHash and yHash into a single unique hash key
     * The hash key is essentially a long integer with the left side xHash and the right side yHash
     */
    private long pairHash(int xHash, int yHash) {
        return ((long)xHash << 32) | (yHash & 0XFFFFFFFFL);
    }
}
