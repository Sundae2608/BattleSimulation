package model.projectile_objects;

public abstract class BaseObject {

    // Positional attributes
    protected double x;
    protected double y;
    protected double height;
    protected boolean alive;
    protected double angle;
    protected boolean impactful;

    public boolean isImpactful() {
        return impactful;
    }
    public void setImpactful(boolean impactful) {
        this.impactful = impactful;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void update() { }
}
