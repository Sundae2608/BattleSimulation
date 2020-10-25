package city_gen_model.algorithms.geometry.house_generation;

import model.settings.MapGenerationMode;

import java.util.HashMap;

public class HouseGenerationSettings {
    private MapGenerationMode mapGenerationMode;
    private double distanceFromEdge;
    private double distanceFromEdgeWiggle;
    private double distanceFromOther;
    private double distanceFromOtherWiggle;
    private double distanceFromCrossRoad;

    // Probability that each new house generated is of certain type.
    private HashMap<HouseType, Double> houseTypeProb;
    private HashMap<HouseType, HouseSizeSettings> houseTypeSizeSettings;

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

    public MapGenerationMode getMapGenerationMode() {
        return mapGenerationMode;
    }
    public void setMapGenerationMode(MapGenerationMode mapGenerationMode) {
        this.mapGenerationMode = mapGenerationMode;
    }

    public HashMap<HouseType, Double> getHouseTypeProb() {
        return houseTypeProb;
    }
    public void setHouseTypeProbs(HashMap houseTypeProb) {
        this.houseTypeProb = houseTypeProb;
    }

    public void setHouseTypeProb(HashMap<HouseType, Double> houseTypeProb) {
        this.houseTypeProb = houseTypeProb;
    }

    public HashMap<HouseType, HouseSizeSettings> getHouseTypeSizeSettings() {
        return houseTypeSizeSettings;
    }

    public void setHouseTypeSizeSettings(HashMap<HouseType, HouseSizeSettings> houseTypeSizeSettings) {
        this.houseTypeSizeSettings = houseTypeSizeSettings;
    }
}
