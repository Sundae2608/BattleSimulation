package model.algorithms.geometry;

import model.settings.MapGenerationMode;

public class HouseGenerationSettings {
    private double distanceFromEdge;
    private double distanceFromEdgeWiggle;
    private double distanceFromOther;
    private double distanceFromOtherWiggle;
    private double distanceFromCrossRoad;
    private double houseWidth;
    private double houseWidthWiggle;
    private double houseArea;
    private double houseAreaWiggle;
    private MapGenerationMode mapGenerationMode;

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

    public double getHouseArea() {
        return houseArea;
    }
    public void setHouseArea(double houseArea) {
        this.houseArea = houseArea;
    }

    public double getHouseAreaWiggle() {
        return houseAreaWiggle;
    }
    public void setHouseAreaWiggle(double houseAreaWiggle) {
        this.houseAreaWiggle = houseAreaWiggle;
    }

    public MapGenerationMode getMapGenerationMode() {
        return mapGenerationMode;
    }
    public void setMapGenerationMode(MapGenerationMode mapGenerationMode) {
        this.mapGenerationMode = mapGenerationMode;
    }
}
