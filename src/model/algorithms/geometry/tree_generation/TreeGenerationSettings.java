package model.algorithms.geometry.tree_generation;

public class TreeGenerationSettings {
    private double distanceFromEdge;
    private double distanceFromEdgeWiggle;
    private double distanceFromOther;
    private double distanceFromOtherWiggle;
    private double distanceFromCrossRoad;

    private double size;
    private double sizeWiggle;

    public double getDistanceFromEdge() {
        return distanceFromEdge;
    }

    public void setDistanceFromEdge(double distanceFromEdge) {
        this.distanceFromEdge = distanceFromEdge;
    }

    public double getDistanceFromEdgeWiggle() {
        return distanceFromEdgeWiggle;
    }

    public void setDistanceFromEdgeWiggle(double distanceFromEdgeWiggle) {
        this.distanceFromEdgeWiggle = distanceFromEdgeWiggle;
    }

    public double getDistanceFromOther() {
        return distanceFromOther;
    }

    public void setDistanceFromOther(double distanceFromOther) {
        this.distanceFromOther = distanceFromOther;
    }

    public double getDistanceFromOtherWiggle() {
        return distanceFromOtherWiggle;
    }

    public void setDistanceFromOtherWiggle(double distanceFromOtherWiggle) {
        this.distanceFromOtherWiggle = distanceFromOtherWiggle;
    }

    public double getDistanceFromCrossRoad() {
        return distanceFromCrossRoad;
    }

    public void setDistanceFromCrossRoad(double distanceFromCrossRoad) {
        this.distanceFromCrossRoad = distanceFromCrossRoad;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getSizeWiggle() {
        return sizeWiggle;
    }

    public void setSizeWiggle(double sizeWiggle) {
        this.sizeWiggle = sizeWiggle;
    }
}
