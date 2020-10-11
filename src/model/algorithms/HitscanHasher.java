package model.algorithms;

import model.projectile_objects.BaseProjectile;
import model.projectile_objects.HitscanObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will hash all related projectiles
 */
public class HitscanHasher {
    private ArrayList<HitscanObject> objects;

    public HitscanHasher() {
        objects = new ArrayList<>();
    }

    /**
     * Add a BaseSingle objects to the space hasher
     */
    public void addObject(HitscanObject obj) {
        objects.add(obj);
    }

    /**
     * Hash all objects into the internal hashMap of the space hasher. Each object will be appended to an array list at
     * a certain key based on its position.
     */
    public void updateObjects() {
        objects.clear();
    }

    /**
     * Getter and setters
     */
    public ArrayList<HitscanObject> getObjects() {
        return objects;
    }
}
