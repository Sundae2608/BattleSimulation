package model.algorithms.geometry;

public class HouseGenerationSettings {
    private double distanceFromEdge;
    private double distanceFromEdgeWiggle;
    private double distanceFromOther;
    private double distanceFromOtherWiggle;
    private double houseWidth;
    private double houseWidthWiggle;
    private double houseLength;
    private double HouseLengthWiggle;

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

    public double getHouseWidth() {
        return houseWidth;
    }
    public void setHouseWidth(double houseWidth) {
        this.houseWidth = houseWidth;
    }

    public double getHouseWidthWiggle() {
        return houseWidthWiggle;
    }
    public void setHouseWidthWiggle(double houseWidthWiggle) {
        this.houseWidthWiggle = houseWidthWiggle;
    }

    public double getHouseLength() {
        return houseLength;
    }
    public void setHouseLength(double houseLength) {
        this.houseLength = houseLength;
    }

    public double getHouseLengthWiggle() {
        return HouseLengthWiggle;
    }
    public void setHouseLengthWiggle(double houseLengthWiggle) {
        HouseLengthWiggle = houseLengthWiggle;
    }
}
