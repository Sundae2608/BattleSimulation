package model.algorithms;

import model.projectile_objects.BaseProjectile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will hash all related projectiles
 * TODO: At the moment, Projectile Hashing is not really doing anything related to its Hash mechanism. Consider
 *  dropping all the hash mechanism and simply creates a container.
 */
public class ProjectileHasher {
    private ArrayList<BaseProjectile> objects;

    public ProjectileHasher() {
        objects = new ArrayList<>();
    }

    /**
     * Add a BaseSingle objects to the space hasher
     */
    public void addObject(BaseProjectile obj) {
        objects.add(obj);
    }

    /**
     * Hash all objects into the internal hashMap of the space hasher. Each object will be appended to an array list at
     * a certain key based on its position.
     */
    public void updateObjects() {
        objects.removeIf(obj -> !obj.isAlive());
    }

    /**
     * Getter and setters
     */
    public ArrayList<BaseProjectile> getObjects() {
        return objects;
    }
}
